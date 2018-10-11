package lir

import util.collection.ShortList

sealed class TailInstruction {
    abstract fun pretty(blockParameterCount: Int) : String
}

class GotoInstruction(
        val blockId: BlockId,
        val blockArguments: Arguments
) : TailInstruction() {
    override fun pretty(blockParameterCount: Int): String = "goto block${blockId.index} ($blockArguments)"
}

class ConditionalJumpInstruction(
        val conditionIndex: InstructionIndex,
        val thenBlockId: BlockId,
        val thenArguments: Arguments,
        val elseBlockId: BlockId,
        val elseArguments: Arguments
) : TailInstruction() {
    override fun pretty(blockParameterCount: Int): String {
        return "jump_if cond(%${conditionIndex.getVariableIndex(blockParameterCount)}) " +
                "block${thenBlockId.index} ($thenArguments) else block${elseBlockId.index} ($elseArguments)"
    }
}

object UnreachableInstruction : TailInstruction() {
    override fun pretty(blockParameterCount: Int): String = "unreachable"
}

class CallInstruction(
        val funcitonId: FunctionId,
        val arguments: Arguments
) : TailInstruction() {
    override fun pretty(blockParameterCount: Int): String = "call $funcitonId ($arguments)"
}

inline class Arguments(private val args: ShortList) {
    operator fun get(index: Int): InstructionIndex = InstructionIndex(args[index])

    val size: Int
        get() = args.size

    override fun toString(): String = args.toString()
}