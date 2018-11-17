package lir


class LirFunction(
    val name: String,
    val instructions: List<LirInstr>,
    val virtualRegistersCount: Int
) {
    override fun toString(): String {
        return buildString {
            append("fun $name :  virtual regs: $virtualRegistersCount\n")
            for ((index, instruction) in instructions.withIndex()) {
                append("  $index $instruction")
                if (index != instructions.lastIndex) {
                    append("\n")
                }
            }
        }
    }
}