package lir

import util.collection.ShortList


sealed class TailInstruction

class GotoInstruction(
        val blockIndex: BlockIndex,
        val blockArguments: Arguments
) : TailInstruction()

class ConditionalJumpInstruction(
        val thenBlockIndex: BlockIndex,
        val thenArguments: Arguments,
        val elseBlockIndex: BlockIndex,
        val elseArguments: Arguments
)

object UnreachableInstruction : TailInstruction()

class CallInstruction(
        val funcitonId: FunctionId,
        val arguments: Arguments
) : TailInstruction()

inline class BlockIndex(val index: Int)

inline class Arguments(private val args: ShortList) {
    operator fun get(index: Int): InstructionIndex = InstructionIndex(args[index])

    val size: Int
        get() = args.size

}

// TODO block id inside?
class BasicBlock(
        val instructions: Array<BBInstruction>,
        val tailInstruction: TailInstruction,
        val parametersCount: Int
)