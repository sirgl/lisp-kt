package tools.codegen

import lir.BBInstruction
import lir.Operand

sealed class InstructionInlineValue {
    /**
     * Size in bytes of inline value
     */
    abstract val size: Int
    /**
     * Required for codegen, type of inline value
     */
    abstract val typeName: String
    /**
     * Required for documentation, human readable name of inline value
     */
    abstract val presentableName: String
    /**
     * Required for codegen, name of inline value
     */
    abstract val propertyName: String
    /**
     * Starts from title case, short description, what does this parameter do
     */
    abstract val description: String

    open val generationNeeded: Boolean
        get() = true
}

abstract class ByteValue : InstructionInlineValue() {
    override val typeName: String
        get() = "Byte"
    override val size: Int
        get() = 1
}

abstract class ShortValue : InstructionInlineValue() {
    override val typeName: String
        get() = "Short"
    override val size: Int
        get() = 2
}

class OpcodeValue() : ByteValue() {
    override val presentableName: String
        get() = "Opcode"
    override val description: String
        get() = "Opcode number"
    override val propertyName: String
        get() = "opcode"
}

class EmptyValue(override val size: Int) : InstructionInlineValue() {
    override val typeName: String
        get() = "Nothing"
    override val presentableName: String
        get() = "Empty"
    override val description: String
        get() = "Meaningless space"
    override val generationNeeded: Boolean
        get() = false
    override val propertyName: String
        get() = "empty"
}

class OperandValue(override val presentableName: String, override val propertyName: String) : InstructionInlineValue() {
    override val size: Int
        get() = 2
    override val typeName: String
        get() = "Short"
    override val description: String
        get() = "Operand, which represents one of the variables declared before in this block"
}

enum class LayoutType {
    Binary,
    Unary,
    IconstInplace,
}

class InstructionLayout(val values: List<InstructionInlineValue>, val type: LayoutType)

class InstructionDescription(
        val opcode: Byte,
        val lowercasedName: String, // lowercased
        val instructionLayout: InstructionLayout,
        val description: String
)

//fun binopLayout(): InstructionLayout {
//    return InstructionLayout(listOf(
//            OpcodeValue()
//    ))
//}


object Instructions {
    val instructions = mutableListOf<InstructionDescription>()
    var nextInstructionIndex = 0


    private fun instruction(name: String, instructionLayout: InstructionLayout, description: String) {
        val opcode = nextInstructionIndex.toByte()
        nextInstructionIndex++
        instructions.add(InstructionDescription(opcode, name, instructionLayout, description))
    }

    private val binaryLayout = InstructionLayout(listOf(
            OpcodeValue(),
            EmptyValue(3),
            OperandValue("Operand 1", "firstOperand"),
            OperandValue("Operand 2", "secondOperand")
    ), LayoutType.Binary)

    private val unaryLayout = InstructionLayout(listOf(
            OpcodeValue(),
            EmptyValue(5),
            OperandValue("Operand", "operand")
    ), LayoutType.Binary)

    private fun binaryInstruction(name: String, description: String) = instruction(name, binaryLayout, description)

    private fun unaryInstruction(name: String, description: String)= instruction(name, unaryLayout, description)

    init {
        binaryInstruction("add", "Integer addition")
        binaryInstruction("sub", "Integer subtraction")
        binaryInstruction("mul", "Integer multiplication")
        binaryInstruction("div", "Integer division. Undefined behavior deleting on zero")
        binaryInstruction("rem", "Take reminder from division. Undefined behavior deleting on zero")
        unaryInstruction("inv", "Bitwise complement of given value")

    }
}

class InstructionBin(private var instrValue: Long) {
    val first: Short
        get() = (instrValue shr 16).toShort()
//        set(value) {
//            val mask = (0xFFFF.toLong() shl 16).inv()
//            val shifted = value.toLong() shl 16
//            val masked = instrValue and mask
//            instrValue = masked or shifted
//        }
}

inline fun <T> BBInstruction.asBinary(block: InstructionBin.() -> T) : T = InstructionBin(this.storage).block()

fun main(args: Array<String>) {
//    Instructions.instructions
//    val bbInstruction = BBInstruction(Opcode(0), Operand(44), Operand(123))
//    bbInstruction.asBinary {
//        first = 44
//    }
//    println(bbInstruction.asBinary { first })
}




