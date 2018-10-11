package lir



object Opcodes : Iterable<OpDescription> {
    private val opcodes = arrayOfNulls<OpDescription>(256)

    private var nextOpcodeNumber = 0

    operator fun get(opcode: Byte) : OpDescription {
        return opcodes[opcode.toInt()]!!
    }

    /**
     * Not optimized for frequent calls, result should be cached
     */
    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<OpDescription> {
        return opcodes.slice(0 until nextOpcodeNumber).iterator() as Iterator<OpDescription>
    }

    private fun nextOpcode() : Byte {
        val opcode = nextOpcodeNumber.toByte()
        nextOpcodeNumber++
        return opcode
    }

    /**
     * Must be invoked after nextOpcode
     * Introduced because of circular dependency between opcode and opcode description
     */
    private fun <T : OpDescription> setDescr(opDescr: T): T {
        opcodes[nextOpcodeNumber - 1] = opDescr
        return opDescr
    }

    private fun binOp(
            name: String,
            leftTypeRequirement: TypeDescriptorMatcher? = null,
            rightTypeRequirement: TypeDescriptorMatcher? = null
    ) : BinaryOpDescription {
        return setDescr(BinaryOpDescription(name, Opcode(nextOpcode()), leftTypeRequirement, rightTypeRequirement))
    }

    private fun i32BinOp(name: String) : BinaryOpDescription = binOp(name, i32Matcher, i32Matcher)

    private fun unOp(
            name: String,
            typeRequirementMatcher: TypeDescriptorMatcher?
    ) : UnaryOpDescription {
        return setDescr(UnaryOpDescription(name, nextOpcode(), typeRequirementMatcher))
    }

    val SUM = i32BinOp("sum")
    val OP_SUM = SUM.opcode
    val MINUS = i32BinOp("minus")
    val OP_MINUS = MINUS.opcode
    val MULTIPLY = i32BinOp("multiply")
    val OP_MULTIPLY = MULTIPLY.opcode
    val DIV = i32BinOp("div")
    val OP_DIV = DIV.opcode
    val REM = i32BinOp("rem")
    val OP_REM = REM.opcode
    val INC = i32BinOp("inc")
    val OP_INC = INC.opcode
    val DEC = i32BinOp("dec")
    val OP_DEC = DEC.opcode
    val BITWISE_OR = i32BinOp("bitwise_or")
    val OP_BITWISE_OR = BITWISE_OR.opcode
    val BITWISE_AND = i32BinOp("bitwise_and")
    val OP_BITWISE_AND = BITWISE_AND.opcode
    val BITWISE_XOR = i32BinOp("bitwise_xor")
    val OP_BITWISE_XOR = BITWISE_XOR.opcode
    val BITWISE_COMPLEMENT = i32BinOp("bitwise_complement")
    val OP_BITWISE_COMPLEMENT = BITWISE_COMPLEMENT.opcode
    val BITCAST = unOp("bitcast", null)
    val OP_BITCAST = BITCAST.opcode
    val GET_ELEMENT_PTR = unOp("get_element_ptr", null)
    val OP_GET_ELEMENT_PTR = GET_ELEMENT_PTR.opcode
}

fun main(args: Array<String>) {
    for (descr in Opcodes) {
        println(descr.presentableDescription)
    }
}


abstract class OpDescription(
        val inlineOperandsCount: Int,
        val name: String,
        val opcode: Opcode
) {
    open val presentableDescription: String
        get() = "$name with $inlineOperandsCount operands and opcode $opcode"
}


class BinaryOpDescription(
        name: String,
        opcode: Opcode,
        val leftTypeRequirement: TypeDescriptorMatcher? = null,
        val rightTypeRequirement: TypeDescriptorMatcher? = null
) : OpDescription(2, name, opcode) {
    override val presentableDescription: String
        get() = buildString {
            // TODO better description
            append(super.presentableDescription)
            if (leftTypeRequirement != null) {
                append("\n")
                append("Left operand: \n\t")
                append(leftTypeRequirement.humanReadableTypeRequirement.quoted())
            }
            if (rightTypeRequirement != null) {
                append("\n")
                append("Right operand: \n\t")
                append(rightTypeRequirement.humanReadableTypeRequirement.quoted())
            }
        }
}

private fun String.quoted(): String = "\"${this}\""


class UnaryOpDescription(
        name: String,
        opcode: Byte,
        val typeRequirement: TypeDescriptorMatcher?
) : OpDescription(1, name, Opcode(opcode)) {
    override val presentableDescription: String
        get() = buildString {
            append(super.presentableDescription)
            if (typeRequirement != null) {
                append("\nOperand: \t")
                append(typeRequirement.humanReadableTypeRequirement.quoted())
            }
        }
}