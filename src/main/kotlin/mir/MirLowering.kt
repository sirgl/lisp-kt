package mir

import hir.*

class MirLowering {
    fun lower(files: List<HirFile>) : List<MirFile> {
        val context = MirBuilderContext()
        return files.map { lower(it, context) }
    }

    private fun lower(file: HirFile, context: MirBuilderContext): MirFile {
        val functions = file.functions.map { lower(it, file, context) }
        return MirFile(file.source, functions)
    }

    private fun lower(function: HirFunctionDeclaration, file: HirFile, context: MirBuilderContext): MirFunction {
        return MirFunctionLowering(function, file, context).lower()
    }
}

private class MirFunctionLowering(
        val function: HirFunctionDeclaration,
        val file: HirFile,
        builderContext: MirBuilderContext
) {
    val builder = MirFunctionBuilder(function.name, function.isMain, builderContext)

    fun lower() : MirFunction {
        if (function.isMain) {
            // TODO add imports
        }
        // TODO add parameter instructions
        lowerExpr(function.body)
        return builder.finishFunction()
    }

    fun lowerExpr(expr: HirExpr) : MirInstrId {
        return when (expr) {
            is HirBlockExpr -> {
                for (stmt in expr.stmts) {
                    lowerStmt(stmt)
                }
                lowerExpr(expr.expr)
            }
            is HirWhileExpr -> TODO()
            is HirAssignExpr -> TODO()
            is HirGlobalCallExpr -> TODO() // probably, I should remove it
            is HirLocalCallExpr -> TODO()
            is HirIfExpr -> TODO()
            is HirBoolLiteral -> builder.emit(MirLoadValueInstr(MirValue.MirBool(expr.value, true)))
            is HirIntLiteral -> builder.emit(MirLoadValueInstr(MirValue.MirInt(expr.value, true)))
            // TODO native strings for c interop
            is HirStringLiteral -> builder.emit(MirLoadValueInstr(MirValue.MirString(expr.value, false)))
            is HirListLiteral -> {
                var listId = builder.emit(MirLoadValueInstr(MirValue.MirEmptyList))
                for (literal in expr.literals) {
                    listId = builder.emit(MirAddElementInstr(lowerExpr(literal), listId))
                }
                listId
            }
            is HirIdentifierLiteral -> builder.emit(MirLoadValueInstr(MirValue.MirSymbol(expr.name)))
            is HirFunctionReference -> {
                TODO()
            }
            is HirVarReference -> TODO()
        }
    }

    fun lowerStmt(stmt: HirStmt) {
        when (stmt) {
            is HirExprStmt -> lowerExpr(stmt.expr)
            is HirVarDeclStmt -> TODO()
        }
    }
}