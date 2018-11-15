package mir

sealed class MirFunction(
        val name: String,
        val parametersCount: Int
) {
    var functionId : Int = -1
}

class MirForeignFunction(name: String, parametersCount: Int) : MirFunction(name, parametersCount) {
    override fun toString(): String {
        return "foreign fun $name params: $parametersCount"
    }
}

class MirFunctionDefinition(
        name: String,
        val blocks: List<MirBasicBlock>,
        private val entryBlockIndex: Short,
        parametersCount: Int,
        val varCount: Short,
        val isMain: Boolean = false
) : MirTypeResolver, MirFunction(name, parametersCount) {
    val entryBlock: MirBasicBlock
    get() = blocks[entryBlockIndex.toInt()]

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
            append("fun $name params: $parametersCount, totalVars: $varCount")
            if (isMain) {
                append(" (main)")
            }
            append("\n")
            append(blocks.joinToString("\n") { it.toString() })
        }
    }
}

interface MirTypeResolver {
    fun resolveResultType(instructionId: MirInstrId) : MirInstrResultType
}