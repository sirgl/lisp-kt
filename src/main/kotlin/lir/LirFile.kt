package lir

import hir.HirMacroasmDefinition
import util.Source

class LirFile(
        val source: Source, val functions: List<LirFunction>,
        val stringTable: Array<String>,
        val macroasms: List<HirMacroasmDefinition>
) {
    override fun toString(): String {
        return buildString {
            if (stringTable.isNotEmpty()) {
                append("String table:\n")
                for ((index, s) in stringTable.withIndex()) {
                    append(String.format("%4d %s\n", index, s))
                }
            }
            append(functions.joinToString("\n\n"))
        }
    }
}