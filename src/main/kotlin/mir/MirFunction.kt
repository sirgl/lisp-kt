package mir

class MirFunction(
        val name: String,
        val blocks: List<MirBasicBlock>,
        private val entryBlockIndex: Short,
        val parametersCount: Int,
        val varCount: Short,
        val isMain: Boolean = false
) : MirTypeResolver {
    val entryBlock: MirBasicBlock
    get() = blocks[entryBlockIndex.toInt()]

    var functionId: Int = -1

    override fun resolveResultType(instructionId: MirInstrId): MirInstrResultType {
        val basicBlockIndex = instructionId.basicBlockIndex.toInt()
        val basicBlock = blocks[basicBlockIndex]
        val instructionArrIndex = instructionId.instructionIndex.toInt()
        return basicBlock.instructions[instructionArrIndex].getReturnType(this)
    }


    fun usages(id: MirInstrId) : List<MirInstrId> {
        val usages = mutableListOf<MirInstrId>()
        for (block in blocks) {
            for (instruction in block.instructions) {
                val dependantIndices = instruction.computeDependantIndices()
                if (id in dependantIndices) {
                    usages.add(instruction.id)
                }
            }
        }
        return usages
    }

    override fun toString(): String {
        return buildString {
            append("fun $name params: $parametersCount, totalVars: $varCount\n")
            append(blocks.joinToString("\n") { it.toString() })
        }
    }
}

interface MirTypeResolver {
    fun resolveResultType(instructionId: MirInstrId) : MirInstrResultType
}