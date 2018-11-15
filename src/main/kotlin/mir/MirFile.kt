package mir

import util.Source

class MirFile(
        val source: Source,
        val functions: List<MirFunction>
) {
    override fun toString(): String {
        return buildString {
            append(source.path + ":\n")
            append(functions.joinToString("\n\n") {it.toString()})
        }
    }
}