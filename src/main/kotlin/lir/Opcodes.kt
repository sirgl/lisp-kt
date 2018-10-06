package lir


object Opcodes : Iterable<OpDescription> {
    val opcodes = arrayOfNulls<OpDescription>(256)

    private var nextOpcodeNumber = 0

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
        return setDescr(BinaryOpDescription(name, nextOpcode(), leftTypeRequirement, rightTypeRequirement))
    }

    private fun i32BinOp(name: String) : BinaryOpDescription = binOp(name, i32Matcher, i32Matcher)

    private fun unOp(
            name: String,
            typeRequirementMatcher: TypeDescriptorMatcher?
    ) : UnaryOpDescription {
        return setDescr(UnaryOpDescription(name, nextOpcode(), typeRequirementMatcher))
    }

    fun getDescription(opcode: Opcode) : OpDescription = opcodes[opcode.storage.toInt()]!!

    val SUM = i32BinOp("sum")
    val OP_SUM = SUM.opcode
    val MINUS = i32BinOp("minus")
    val OP_MINUS = MINUS.opcode
    val MULTIPLY = i32BinOp("multiply")
    val OP_MULTIPLY = MULTIPLY.opcode
    val DIV = i32BinOp("div")
    val OP_DIV = DIV.opcode
    // TODO all last instructions

//    val DEREF
//    val OP_DEREF

}

fun main(args: Array<String>) {
    for (descr in Opcodes) {
        println(descr.presentableDescription)
    }
}


abstract class OpDescription(
        val inlineOperandsCount: Int,
        val name: String,
        val opcode: Byte
) {
    open val presentableDescription: String
        get() = "$name with $inlineOperandsCount operands and opcode $opcode"
}


class BinaryOpDescription(
        name: String,
        opcode: Byte,
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
) : OpDescription(1, name, opcode) {
    override val presentableDescription: String
        get() = buildString {
            append(super.presentableDescription)
            if (typeRequirement != null) {
                append("\nOperand: \t")
                append(typeRequirement.humanReadableTypeRequirement.quoted())
            }
        }
}