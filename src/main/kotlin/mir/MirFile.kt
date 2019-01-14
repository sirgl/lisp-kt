package mir

import hir.HirMacroasmDefinition
import util.Source

class MirFile(
        val source: Source,
        val functions: List<MirFunction>,
        val macroasms: List<HirMacroasmDefinition>
) {
    override fun toString(): String {
        return buildString {
            append(source.path + ":\n")
            append(functions.joinToString("\n\n") {it.toString()})
        }
    }
}