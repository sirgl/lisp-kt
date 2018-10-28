package lir

import lir.types.TypeStorage
import util.io.Path

class LirCompilationUnit(
        val sourceFile: Path,
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