package lir

import util.Source

class LirFile(val source: Source, val functions: List<LirFunction>) {
    override fun toString(): String {
        return functions.joinToString("\n\n")
    }
}