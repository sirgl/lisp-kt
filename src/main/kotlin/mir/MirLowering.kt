package mir

import hir.*
import java.lang.UnsupportedOperationException

class MirLowering {
    fun lower(files: List<HirFile>) : List<MirFile> {
        val context = MirBuilderContext()
        // TODO lower it in correct order, otherwise some cross file declaration uses may fail to lower
        return files.map { lower(it, context) }
    }

    private fun lower(file: HirFile, context: MirBuilderContext): MirFile {
        val functions = file.functions.flatMap {
            val function = lower(it, file, context)

            if (it.isMain) {
                if (it.isEntry) {
                    listOf(function, createEntryWrapper(function.functionId, context))
                } else {
                    listOf(function)
                }
            } else {
                val satellite = createSatelliteRepackager(it, function.functionId, context)
            listOf(function, satellite)
            }
        }
        return MirFile(file.source, functions)
    }

    private fun lower(function: HirFunctionDeclaration, file: HirFile, context: MirBuilderContext): MirFunction {
        return when (function) {
            is HirFunctionDefinition -> MirFunctionLowering(function, file, context).lower()
            is HirNativeFunctionDeclaration -> {
                val id = context.nextFunctionId()
                context.addFunction(function, id)
                val foreignFunction = MirForeignFunction(function.runtimeName, function.parameters.size)
                foreignFunction.functionId = id
                foreignFunction
            }
            else -> throw UnsupportedOperationException()
        }
    }
}

private class MirFunctionLowering(
        val function: HirFunctionDefinition,
        val file: HirFile,
        builderContext: MirBuilderContext
) {
    val builder = MirFunctionBuilder(function.name, function.isMain, builderContext)

    fun lower() : MirFunction {
        if (function.isMain) {
            // TODO add imports
        }
        for (parameter in function.parameters) {
            builder.addVariable(parameter)
        }
        val result = lowerExpr(function.body)
        builder.emit(MirReturnInstruction(result))
        builder.finishBlock()
        return builder.finishFunction(function)
    }

    fun lowerExpr(expr: HirExpr) : MirInstrId {
        return when (expr) {
            is HirBlockExpr -> {
                for (stmt in expr.stmts) {
                    lowerStmt(stmt)
                }
                lowerExpr(expr.expr)
            }
            is HirWhileExpr -> lowerWhile(expr)
            is HirAssignExpr -> builder.emit(MirStoreInstr(builder.getVarId(expr.decl), lowerExpr(expr.rValue)))
            is HirLocalCallExpr -> lowerCall(expr)
            is HirIfExpr -> lowerIf(expr)
            is HirBoolLiteral -> builder.emit(MirLoadValueInstr(MirValue.MirBool(expr.value, true)))
            is HirIntLiteral -> builder.emit(MirLoadValueInstr(MirValue.MirInt(expr.value, true)))
            // TODO native strings for c interop
            is HirStringLiteral -> builder.emit(MirLoadValueInstr(MirValue.MirString(expr.value, false)))
            is HirListLiteral -> {
                var listId = builder.emit(MirLoadValueInstr(MirValue.MirEmptyList))
                for (literal in expr.literals.reversed()) {
                    listId = builder.emit(MirWithElementInstr(lowerExpr(literal), listId))
                }
                listId
            }
            is HirIdentifierLiteral -> builder.emit(MirLoadValueInstr(MirValue.MirSymbol(expr.name)))
            is HirFunctionReference -> {
                val functionId = builder.getFunctionId(expr.decl.satelliteName)
                builder.emit(MirGetFunctionReference(functionId))
            }
            is HirVarReference -> builder.emit(MirLoadInstr(builder.getVarId(expr.decl)))
            is HirCallByReferenceExpr -> {
                val funcRefInstrId = lowerExpr(expr.funcReferenceSource)
                val args = Array(expr.args.size) { index -> lowerExpr(expr.args[index]) }
                var argList = builder.emit(MirLoadValueInstr(MirValue.MirEmptyList))
                for (arg in args.reversed()) {
                   argList = builder.emit(MirWithElementInstr(arg, argList))
                }
                builder.emit(MirCallByRefInstr(funcRefInstrId, argList))
            }
        }
    }

    // pack varargs into list
    private fun prepareArgs(args: List<HirExpr>, decl: HirFunctionDeclaration) : Array<MirInstrId> {
        return Array(decl.parameters.size) { index ->
            val lastIndex = decl.parameters.lastIndex
            // all tail arguments packed into list
            if (index == lastIndex && decl.hasVararg()) {
                var listId = builder.emit(MirLoadValueInstr(MirValue.MirEmptyList))
                for (i in (lastIndex until args.size)) {
                    listId = builder.emit(MirWithElementInstr(lowerExpr(args[i]), listId))
                }
                listId
            } else {
                lowerExpr(args[index])
            }
        }
    }

    private fun lowerCall(expr: HirLocalCallExpr): MirInstrId {
        val args = expr.args
        val exprIds = prepareArgs(args, expr.decl)
        val functionId = builder.getFunctionId(expr.decl)
        return builder.emit(MirLocalCallInstr(functionId, exprIds))
    }

    private fun lowerWhile(expr: HirWhileExpr): MirInstrId {
        // condition
        val conditionId = lowerExpr(expr.condition)
        val untaggedCondition = builder.emit(MirUntagInstruction(conditionId))
        val conditionJumpInstr = MirCondJumpInstruction(untaggedCondition)
        builder.emit(conditionJumpInstr)
        val conditionBlock = builder.finishBlock()

        // body
        lowerExpr(expr.body)
        val jumpBackToCondition = MirGotoInstruction()
        jumpBackToCondition.basicBlockIndex = conditionBlock
        builder.emit(jumpBackToCondition)
        val bodyBlockId = builder.finishBlock()

        // setup jump targets
        val afterWhileBlockId = builder.currentBlockId()
        conditionJumpInstr.thenBlockIndex = bodyBlockId
        conditionJumpInstr.elseBlockIndex = afterWhileBlockId

        //result of while loop is empty list
        return builder.emit(MirLoadValueInstr(MirValue.MirEmptyList))
    }

    private fun lowerIf(expr: HirIfExpr): MirInstrId {
        // condition
        val conditionId = lowerExpr(expr.condition)
        val untaggedCondition = builder.emit(MirUntagInstruction(conditionId))
        val conditionJumpInstr = MirCondJumpInstruction(untaggedCondition)
        builder.emit(conditionJumpInstr)
        builder.finishBlock()
        // if is expression, so we need to return result
        // In both branches we compute expressions and assign it to newly created merge variable
        val mergeVarName = builder.nextIfMergeVarName()
        val mergeVar = object : HirVarDeclaration {
            override val name: String = mergeVarName
        }
        val mergeVarIndex = builder.addVariable(mergeVar)

        // then branch
        val thenBlockId = builder.currentBlockId()
        val thenBranchResultId = lowerExpr(expr.thenBranch)
        val thenGotoEnd = MirGotoInstruction()
        builder.emit(MirStoreInstr(mergeVarIndex, thenBranchResultId))
        builder.emit(thenGotoEnd)
        builder.finishBlock()

        // else branch
        val elseBlockId = builder.currentBlockId()
        val elseBranchResultId = lowerExpr(expr.elseBranch)
        val elseGotoEnd = MirGotoInstruction()
        builder.emit(MirStoreInstr(mergeVarIndex, elseBranchResultId))
        builder.emit(elseGotoEnd)
        builder.finishBlock()

        // setup jump targets
        val afterIfBlockId = builder.currentBlockId()
        thenGotoEnd.basicBlockIndex = afterIfBlockId
        elseGotoEnd.basicBlockIndex = afterIfBlockId
        conditionJumpInstr.thenBlockIndex = thenBlockId
        conditionJumpInstr.elseBlockIndex = elseBlockId

        //merged paths variable
        return builder.emit(MirLoadInstr(mergeVarIndex))
    }

    fun lowerStmt(stmt: HirStmt) {
        when (stmt) {
            is HirExprStmt -> lowerExpr(stmt.expr)
            is HirVarDeclStmt -> {
                builder.addVariable(stmt)
                val initializerId = lowerExpr(stmt.initializer)
                builder.emit(MirStoreInstr(builder.getVarId(stmt), initializerId))
            }
        }
    }
}

/**
 * Create function-satellite, that will repack arguments (checking size) from list and call original function
 * Consider, that first item in list is first argument
 */
fun createSatelliteRepackager(function: HirFunctionDeclaration, originalFunctionId: Int, context: MirBuilderContext): MirFunction {
    val builder = MirFunctionBuilder(function.satelliteName, false, context)
    val parameter = HirParameter("listParameters", false)
    val parameterListVarIndex = builder.addVariable(parameter)
    val parameterListId = builder.emit(MirLoadInstr(parameterListVarIndex))
    val realParameterCountId = builder.emit(MirListSizeInstruction(parameterListId))
    val declaredParameterCount = function.parameters.size
    val expectedParametersCountId = builder.emit(MirLoadValueInstr(MirValue.MirInt(declaredParameterCount, true)))
    val operation = if (function.hasVararg()) {
        MirBinaryOpType.Ge
    } else {
        MirBinaryOpType.Eq
    }
    val conditionId = builder.emit(MirBinaryIntInstr(operation, realParameterCountId, expectedParametersCountId))
    // true - ok path, else - wrong count of parameters
    val untaggedConditionId = builder.emit(MirUntagInstruction(conditionId))
    val conditionalJumpInstr = MirCondJumpInstruction(untaggedConditionId)
    builder.emit(conditionalJumpInstr)
    builder.finishBlock()

    // ok path
    val parameterCount = if (function.hasVararg()) {
        declaredParameterCount - 1
    } else {
        declaredParameterCount
    }
    val parameterIds = mutableListOf<MirInstrId>()
    var currentParameterListId = parameterListId
    for(i in 0 until parameterCount) {
        parameterIds.add(builder.emit(MirListFirstInstruction(currentParameterListId)))
        currentParameterListId = builder.emit(MirListTailInstruction(currentParameterListId))
    }
    if (function.hasVararg()) {
        parameterIds.add(currentParameterListId)
    }
    val callResultId = builder.emit(MirLocalCallInstr(originalFunctionId, parameterIds.toTypedArray()))
    builder.emit(MirReturnInstruction(callResultId))
    val thenBlockId = builder.finishBlock()
    conditionalJumpInstr.thenBlockIndex = thenBlockId

    // error path
    val errorTextId = builder.emit(MirLoadValueInstr(MirValue.MirString("Unexpected parameter count", false)))
    builder.emit(MirPrintErrorAndExitInstruction(errorTextId))
    val elseBlockId = builder.finishBlock()
    conditionalJumpInstr.elseBlockIndex = elseBlockId

    val satelliteDeclaration = object : HirFunctionDeclaration {
        override val parameters = listOf(parameter)
        override val name= function.satelliteName
    }
    return builder.finishFunction(satelliteDeclaration)
}

fun createEntryWrapper(originalFunctionId: Int, context: MirBuilderContext) : MirFunction {
    val builder = MirFunctionBuilder("__entry__", false, context)
    builder.emit(MirLocalCallInstr(originalFunctionId, emptyArray()))
    builder.finishBlock()
    return builder.finishFunction(object: HirFunctionDeclaration {
        override val parameters: List<HirParameter>
            get() = emptyList()
        override val name: String
            get() = "__entry__"
    })
}