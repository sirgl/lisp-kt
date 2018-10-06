package lir

import util.LongStorage


/**
 * @param storage has next layout: | empty 2 byte | tag 2 byte | block/function index 4 byte
 */
inline class JumpInstruction(private val storage: LongStorage) {
    constructor(tag: Short, value: Int) : this(LongStorage(0, tag, value))

    /**
     * [blockOfFunctionIndex] index of basic block or function index, to which control will be transferred
     */
    val blockOfFunctionIndex: Int
        get() = storage.second

    companion object {
        fun makeReturn() = JumpInstruction(RETURN_TAG, 0)
        fun makeJump(blockIndex: Int) = JumpInstruction(JUMP_TAG, blockIndex)
        fun makeCall(functionId: FunctionId) = JumpInstruction(CALL_TAG, functionId.id)
    }

    val tag: Short
        get() = storage.cd

    val isReturn: Boolean
        get() = tag == RETURN_TAG
}

class BasicBlock(
        val instructions: Array<BBInstruction>,
        val tailJumpInstruction: JumpInstruction
)

private const val CALL_TAG = 1.toShort()
private const val JUMP_TAG = 2.toShort()
private const val RETURN_TAG = 0.toShort()