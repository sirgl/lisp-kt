package mir

class MirBasicBlock(
        val index: Short,
        val instructions: MutableList<MirInstr>
) {
    lateinit var function: MirFunction

    fun fireInstructionChanged() {
        for (instruction in instructions) {
            instruction.recomputeType()
        }
    }

    override fun toString(): String {
        return buildString {
            append("b$index:\n")
            append(instructions.joinToString(separator = "\n") { "  $it"})
        }
    }
}

class MirInstrId(
        val basicBlockIndex: Short,
        val instructionIndex: Short
) {
    override fun toString(): String = "b$basicBlockIndex:$instructionIndex"
}