package lir

import lir.types.*
import util.collection.intListOf

inline class BlockId(val index: Int) {
    override fun toString(): String = index.toString()
}

// TODO use here primitive list for instructions
/**
 * @param typeIndexList indices of types from [TypeStorage]. Starts from parameter types
 */
class BasicBlock(
        val instructions: Array<BBInstruction>,
        val typeIndexList: TypeIndexList,
        val tailInstruction: TailInstruction,
        val parametersCount: Int
) {
    /**
     * Intended to set only inside BlockTable
     */
    var id = BlockId(-1)

    override fun toString(): String {
        return "block$id parameters: $parametersCount \n${instructions.joinToString("\n") { "\t" + it }}\n"
    }

    fun pretty(typeStorage: TypeStorage): String {
        return buildString {
            append("block$id (")
            appendParametersText(typeStorage)
            append(")")
            append("\n")
            appendInstructions(typeStorage)
            append("\t")
            append(tailInstruction.pretty(parametersCount))
        }
    }

    fun nextBlockIndicesInsideFunction(): IntArray {
        return when (tailInstruction) {
            is GotoInstruction -> intArrayOf(tailInstruction.blockId.index)
            is ConditionalJumpInstruction -> intArrayOf(tailInstruction.thenBlockId.index, tailInstruction.elseBlockId.index)
            else -> intArrayOf()
        }
    }

    private fun StringBuilder.appendInstructions(typeStorage: TypeStorage) {
        for (instructionIndex in 0 until instructions.size) {
            val instruction = instructions[instructionIndex]
            append("\t")
            append("%")
            val variableIndex = instructionIndex + parametersCount
            append(variableIndex)
            append(": ")
            val type = getVariableType(variableIndex, typeStorage)
            append(type.typeDescriptor.presentableName)
            append(" = ")
            append(instruction)
            append("\n")
        }
    }

    private fun StringBuilder.appendParametersText(typeStorage: TypeStorage) {
        for (i in 0 until parametersCount) {
            val type = getVariableType(i, typeStorage)
            append("%").append(i).append(": ").append(type.typeDescriptor.presentableName)
        }
    }

    private fun getVariableType(index: Int, typeStorage: TypeStorage): LirType {
        val typeIndex = typeIndexList[index]
        return typeStorage[typeIndex]
    }
}

fun main(args: Array<String>) {
//    val storage = TypeStorage()
//    storage.addType(I32Type)
//    storage.addType(I8Type)
//    val block = BasicBlock(arrayOf(
//            BBInstruction(Opcodes.OP_DEC, Operand(0)),
//            BBInstruction(Opcodes.OP_INC, Operand(1))
//    ), TypeIndexList(intListOf(0, 0, 0)), UnreachableInstruction, 1)
//    block.id = BlockId(0)
//    println(block.pretty(storage))
}