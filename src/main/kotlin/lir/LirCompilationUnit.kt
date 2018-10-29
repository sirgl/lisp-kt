package lir

import lir.types.TypeStorage
import util.Source

class LirCompilationUnit(
        val sourceFile: Source,
        val functions: List<LirFunction>,
        val globalVars: List<LirVar>,
        val typeStorage: TypeStorage
) {

    override fun toString(): String {
        return buildString {
            append(globalVars.joinToString("\n") { it.toString() })
            append(functions.joinToString("\n") { it.pretty(typeStorage) })
        }
    }
}