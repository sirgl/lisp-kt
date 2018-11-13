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
}

class MirInstrId(
        val basicBlockIndex: Short,
        val instructionIndex: Short
)