package mir

/**
 * instruction memoizes result type until it is invalidated (e. g. due to optimization)
 */
sealed class MirInstr {
    private var memo: Memo? = null

    lateinit var id: MirInstrId

    protected open fun computeReturnType(resolver: MirResolver) : MirInstrResultType = MirInstrResultType.NoType

    /**
     *  Indices of values, which result type is dependant on
     */
    open fun computeDependantIndices() : Array<MirInstrId> = arrayOf()

    fun getReturnType(resolver: MirResolver): MirInstrResultType {
        if (memo == null) {

            val resultType = computeReturnType(resolver)
            val dependantIndices = computeDependantIndices()
            val dependantTypes = Array(dependantIndices.size) {
                index -> resolver.resolveResultType(dependantIndices[index])
            }
            memo = Memo(resultType, dependantTypes)
        }
        // TODO actually, here I should check, that dependant types are the same
        return memo!!.resultType
    }

    fun recomputeType() {
        memo = null
    }

    private class Memo(val resultType: MirInstrResultType, val dependantTypes: Array<MirInstrResultType>)

    fun replaceWith(newInstr: MirInstr, block: MirBasicBlock) {
        block.instructions[findIndex(block)] = newInstr
        block.fireInstructionChanged()
    }

    fun findIndex(block: MirBasicBlock): Int {
        val instructions = block.instructions
        for ((index, instruction) in instructions.withIndex()) {
            if (this === instruction) {
                return index
            }
        }
        return -1
    }
}

/**
 *  Instruction which has explicit value, that can be used as operand
 */
sealed class MirValueInstr : MirInstr()

sealed class MirTagAddInstr : MirInstr()

object MirAddIntTagInstr : MirTagAddInstr()
object MirAddBoolTagInstr : MirTagAddInstr()

// TODO is it required?
object MirAddObjTagInstr : MirTagAddInstr()

// TODO untag?


sealed class MirBinaryIntInstr(val opType: MirBinaryOpType) : MirInstr()

class MirBinaryImmediateRightInstr(
        opType: MirBinaryOpType,
        val leftId: MirInstrId
) : MirBinaryIntInstr(opType) {
    override fun computeDependantIndices(): Array<MirInstrId> {
        return arrayOf(leftId)
    }
}

class MirBinaryImmediateLeftInstr(
        opType: MirBinaryOpType,
        val rightId: MirInstrId
) : MirBinaryIntInstr(opType) {
    override fun computeDependantIndices(): Array<MirInstrId> {
        return arrayOf(rightId)
    }
}

class MirBinaryImmediateFullInstr(
        opType: MirBinaryOpType,
        val leftId: MirInstrId,
        val rightId: MirInstrId
) : MirBinaryIntInstr(opType) {
    override fun computeDependantIndices(): Array<MirInstrId> {
        return arrayOf(leftId, rightId)
    }
}

enum class MirBinaryOpType {
    Add,
    Sub,
    Rem,
    Div,
    Mul,

    Gt,
    Ge,
    Eq,
    Lt,
    Le,

    Or,
    And
}

class MirLoadValueInstr(value: MirValue) : MirValueInstr()

sealed class MirValue {
    class MirInt(val value: Int, val tagged: Boolean) : MirValue()
    class MirBool(val value: Boolean, val tagged: Boolean) : MirValue()
    class MirString(val value: String, val isNative: Boolean) : MirValue()
    class MirSymbol(val value: String) : MirValue()
    object MirEmptyList : MirValue()
}

class MirAddElementInstr(val value: MirInstrId, val listId: MirInstrId) : MirValueInstr()

class MirGetFunctionReference(val functionId: Int): MirValueInstr()

// TODO all other instructions


sealed class MirTailInstruction : MirInstr()

class MirGotoInstruction(val basicBlockIndex: Int) : MirTailInstruction()

class MirCondJumpInstruction(
        val conditionId: MirInstrId,
        val thenBlockIndex: Short,
        val elseBlockIndex: Short
) : MirTailInstruction()
