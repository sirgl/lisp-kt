package mir

/**
 * instruction memoizes result type until it is invalidated (e. g. due to optimization)
 */
sealed class MirInstr {
    private var memo: Memo? = null

    lateinit var id: MirInstrId

    protected open fun computeReturnType(resolver: MirTypeResolver): MirInstrResultType = MirInstrResultType.NoType

    /**
     *  Indices of values, which result type is dependant on
     */
    open fun computeDependantIndices(): Array<MirInstrId> = arrayOf()

    fun getReturnType(resolver: MirTypeResolver): MirInstrResultType {
        if (memo == null) {

            val resultType = computeReturnType(resolver)
            val dependantIndices = computeDependantIndices()
            val dependantTypes = Array(dependantIndices.size) { index ->
                resolver.resolveResultType(dependantIndices[index])
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

    abstract fun pretty(strategy: PrettyPrintStrategy): String

    override fun toString(): String = pretty(defaultPrintStrategy)
}

/**
 *  Instruction which has explicit value, that can be used as operand
 */
sealed class MirValueInstr : MirInstr()

sealed class MirTagAddInstr : MirInstr() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return "tag"
    }
}

object MirAddIntTagInstr : MirTagAddInstr() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return super.pretty(strategy) + " int"
    }
}

object MirAddBoolTagInstr : MirTagAddInstr() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return super.pretty(strategy) + " bool"
    }
}

// TODO is it required?
object MirAddObjTagInstr : MirTagAddInstr() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return super.pretty(strategy) + " obj"
    }
}

// TODO untag?


sealed class MirBinaryIntInstr(
        val opType: MirBinaryOpType,
        val leftId: MirInstrId,
        val rightId: MirInstrId
) : MirInstr() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return "binary $opType ${strategy.instrIdRenderer.render(leftId)}, ${strategy.instrIdRenderer.render(rightId)}"
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

class MirLoadValueInstr(val value: MirValue) : MirValueInstr() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return "load_const $value"
    }
}

sealed class MirValue {

    class MirInt(val value: Int, val tagged: Boolean) : MirValue() {
        override fun toString(): String {
            return printValue(value.toString(), "i32", tagged)
        }
    }

    class MirBool(val value: Boolean, val tagged: Boolean) : MirValue() {
        override fun toString(): String {
            return printValue(value.toString(), "bool", tagged)
        }
    }
    class MirString(val value: String, val isNative: Boolean) : MirValue() {
        override fun toString(): String {
            return printValue(value, "string", !isNative)
        }
    }
    class MirSymbol(val value: String) : MirValue() {
        override fun toString(): String {
            return printValue(value, "symbol", false)
        }
    }
    object MirEmptyList : MirValue() {
        override fun toString(): String {
            return "()"
        }
    }

    protected fun printValue(value: String, type: String, tagged: Boolean) : String {
        return buildString {
            append("$value ($type")
            if (tagged) {
                append(", tagged")
            }
            append(")")
        }
    }
}

class MirWithElementInstr(val valueId: MirInstrId, val listId: MirInstrId) : MirValueInstr() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return "with_element list: ${strategy.instrIdRenderer.render(valueId)}, value: ${strategy.instrIdRenderer.render(valueId)}"
    }

    override fun computeDependantIndices(): Array<MirInstrId> {
        return arrayOf(valueId, listId)
    }
}

class MirGetFunctionReference(val functionId: Int) : MirValueInstr() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return "get_function_reference ${strategy.functionIdRenderer.render(functionId)}"
    }
}

class MirStoreInstr(val varId: Short, var valueId: MirInstrId) : MirValueInstr() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return "store_var: ${strategy.varIdRenderer.render(varId)} value: ${strategy.instrIdRenderer.render(valueId)}"
    }

    override fun computeDependantIndices(): Array<MirInstrId> {
        return arrayOf(valueId)
    }
}

class MirLoadInstr(val varId: Short) : MirValueInstr() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return "load_var: ${strategy.varIdRenderer.render(varId)}"
    }

}

sealed class MirCallInstr(val args: Array<MirInstrId>) : MirValueInstr()

class MirCallByRefInstr(val referenceInstrId: MirInstrId, args: Array<MirInstrId>) : MirCallInstr(args) {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        val instrIdRenderer = strategy.instrIdRenderer
        val args = args.joinToString(separator = ", ", prefix = "(", postfix = ")") { instrIdRenderer.render(it) }
        return "call_by_reference referneceInstr: ${strategy.instrIdRenderer.render(referenceInstrId)} args: $args"
    }

}

class MirLocalCallInstr(val functionId: Int, args: Array<MirInstrId>) : MirCallInstr(args) {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        val instrIdRenderer = strategy.instrIdRenderer
        val args = args.joinToString(separator = ", ", prefix = "(", postfix = ")") { instrIdRenderer.render(it) }
        return "call function: ${strategy.functionIdRenderer.render(functionId)} args: $args"
    }

    override fun computeDependantIndices(): Array<MirInstrId> {
        return args
    }
}

// TODO all other instructions


sealed class MirTailInstruction : MirInstr()

class MirReturnInstruction(val instrValueToReturn: MirInstrId) : MirTailInstruction() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return "return ${strategy.instrIdRenderer.render(instrValueToReturn)}"
    }
}

class MirGotoInstruction : MirTailInstruction() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        return "goto ${strategy.blockIndexRenderer.render(basicBlockIndex)}"
    }

    var basicBlockIndex: Short = -1
}

class MirCondJumpInstruction(
        val conditionId: MirInstrId
) : MirTailInstruction() {
    override fun pretty(strategy: PrettyPrintStrategy): String {
        val condition = strategy.instrIdRenderer.render(conditionId)
        val blockIndexRenderer = strategy.blockIndexRenderer
        val thenBlock = blockIndexRenderer.render(thenBlockIndex)
        val elseBlock = blockIndexRenderer.render(elseBlockIndex)
        return "cond_jump cond: $condition then: $thenBlock else: $elseBlock"
    }

    var thenBlockIndex: Short = -1
    var elseBlockIndex: Short = -1
}
