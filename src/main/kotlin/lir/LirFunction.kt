package lir


class LirFunction(
    val name: String,
    val instructions: List<LirInstr>,
    val virtualRegistersCount: Int,
    val parameterCount: Int
) {
    override fun toString(): String {
        return buildString {
            append("fun $name :  virtual regs: $virtualRegistersCount paramCount: $parameterCount\n")
            for ((index, instruction) in instructions.withIndex()) {
                append("  $index $instruction")
                if (index != instructions.lastIndex) {
                    append("\n")
                }
            }
        }
    }
}