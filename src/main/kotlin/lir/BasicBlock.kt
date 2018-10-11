package lir

import util.collection.ShortList


sealed class TailInstruction {
    abstract fun pretty(blockParameterCount: Int) : String
}

class GotoInstruction(
        val blockIndex: BlockIndex,
        val blockArguments: Arguments
) : TailInstruction() {
    override fun pretty(blockParameterCount: Int): String = "goto block${blockIndex.index} ($blockArguments)"
}

class ConditionalJumpInstruction(
        val conditionIndex: InstructionIndex,
        val thenBlockIndex: BlockIndex,
        val thenArguments: Arguments,
        val elseBlockIndex: BlockIndex,
        val elseArguments: Arguments
) : TailInstruction() {
    override fun pretty(blockParameterCount: Int): String {
        return "jump_if cond(%${conditionIndex.getVariableIndex(blockParameterCount)}) " +
                "block${thenBlockIndex.index} ($thenArguments) else block${elseBlockIndex.index} ($elseArguments)"
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

inline class BlockIndex(val index: Int)

inline class Arguments(private val args: ShortList) {
    operator fun get(index: Int): InstructionIndex = InstructionIndex(args[index])

    val size: Int
        get() = args.size

    override fun toString(): String = args.toString()
}

// TODO block id inside?
class BasicBlock(
        val instructions: Array<BBInstruction>,
        val tailInstruction: TailInstruction,
        val parametersCount: Int
)