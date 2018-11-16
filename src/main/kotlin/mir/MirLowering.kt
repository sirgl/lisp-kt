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
        val functions = file.functions.map { lower(it, file, context) }
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
                for (literal in expr.literals) {
                    listId = builder.emit(MirWithElementInstr(lowerExpr(literal), listId))
                }
                listId
            }
            is HirIdentifierLiteral -> builder.emit(MirLoadValueInstr(MirValue.MirSymbol(expr.name)))
            is HirFunctionReference -> {
                val functionId = builder.getFunctionId(expr.decl)
                builder.emit(MirGetFunctionReference(functionId))
            }
            is HirVarReference -> builder.emit(MirLoadInstr(builder.getVarId(expr.decl)))
        }
    }

    private fun lowerCall(expr: HirLocalCallExpr): MirInstrId {
        val args = expr.args
        val exprIds = Array(args.size) { index -> lowerExpr(args[index]) }
        val functionId = builder.getFunctionId(expr.decl)
        return builder.emit(MirCallInstr(functionId, exprIds))
    }

    private fun lowerWhile(expr: HirWhileExpr): MirInstrId {
        // condition
        val conditionId = lowerExpr(expr.condition)
        val conditionJumpInstr = MirCondJumpInstruction(conditionId)
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
        val conditionJumpInstr = MirCondJumpInstruction(conditionId)
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