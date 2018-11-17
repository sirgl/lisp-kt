package lir

fun LirFunction.toBBGraph() : String {
    val jumpTargets = (collectJumpTargets() + listOf(0, instructions.size)).distinct().sorted()
    val basicBlocks = buildBasicBlocks(jumpTargets)

    return buildString {
        append("digraph $name {\n")
        append("start -> 0\n")
        for (basicBlock in basicBlocks) {
            val instructionsText = basicBlock.instructions.joinToString("\n")
            val index = basicBlock.startIndex
            append("$index [shape=box, label=\"$instructionsText\"]\n")
            val last = basicBlock.instructions.last()
            when (last) {
                is LirCondJumpInstr -> append("$index -> ${last.thenInstructionIndex}\n$index -> ${last.elseInstrIndex}\n")
                is LirReturnInstr -> append("$index -> end\n")
                is LirGotoInstr -> append("$index -> ${last.instrIndex}\n")
                else -> append("$index -> ${index + basicBlock.instructions.size}\n")
            }
        }
        append("}")
    }
}

private fun LirFunction.buildBasicBlocks(jumpTargets: List<Int>): List<BasicBlock> {
    if (jumpTargets.isEmpty()) {
        return listOf(BasicBlock(instructions, 0))
    }
    val basicBlocks = mutableListOf<BasicBlock>()
    for (i in 1 until jumpTargets.size) {
        val startIndex = jumpTargets[i - 1]
        val endIndex = jumpTargets[i]
        val basicBlock = instructions.subList(startIndex, endIndex)
        basicBlocks.add(BasicBlock(basicBlock, startIndex))
    }
    return basicBlocks
}

private class BasicBlock(val instructions: List<LirInstr>, val startIndex: Int)

private fun LirFunction.collectJumpTargets(): MutableList<Int> {
    val jumpTargets = mutableListOf<Int>()
    for (instruction in instructions) {
        when (instruction) {
            is LirGotoInstr -> {
                jumpTargets.add(instruction.instrIndex)
            }
            is LirCondJumpInstr -> {
                jumpTargets.add(instruction.thenInstructionIndex)
                jumpTargets.add(instruction.elseInstrIndex)
            }
        }
    }
    return jumpTargets
}