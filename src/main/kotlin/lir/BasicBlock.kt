package lir

import lir.types.LirType
import lir.types.TypeIndexList
import lir.types.TypeStorage

inline class BlockId(val index: Int) {
    override fun toString(): String = index.toString()
}

/**
 * @param typeIndexList indices of types from [TypeStorage]. Starts from parameter types
 */
class BasicBlock(
        val instructions: Array<BBInstruction>,
        val typeIndexList: TypeIndexList,
        val tailInstruction: TailInstruction,
        val parametersCount: Int,
        val id: BlockId
) {
    override fun toString(): String {
        return "block$id parameters: $parametersCount \n${instructions.joinToString("\n") { "\t" + it }}\n"
    }

    fun pretty(typeStorage: TypeStorage): String {
        return buildString {
            append("block$id ")
            append(parametersText(typeStorage))
            append(instructions.joinToString("\n") { "\t" + it })
            append(tailInstruction.pretty(parametersCount))
        }
    }

    fun parametersText(typeStorage: TypeStorage) : String = buildString {
        for (i in 0 until parametersCount) {
            val type = getVariableType(i, typeStorage)
            append("%").append(i).append(": ").append(type)
        }
    }

    private fun getVariableType(index: Int, typeStorage: TypeStorage): LirType {
        val typeIndex = typeIndexList[index]
        return typeStorage[typeIndex]
    }
}