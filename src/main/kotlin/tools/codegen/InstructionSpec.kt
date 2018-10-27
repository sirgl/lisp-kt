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

object OpcodeValue : ByteValue() {
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


object ArgsCountValue : InstructionInlineValue() {
    override val size: Int
        get() = 1
    override val typeName: String
        get() = "Byte"
    override val description: String
        get() = "Count of arguments hold inside this instruction"
    override val presentableName: String
        get() = "count of arguments inside"
    override val propertyName: String
        get() = "argsHolded"
}

object InplaceI32Value : InstructionInlineValue() {
    override val size: Int
        get() = 4
    override val typeName: String
        get() = "Int"
    override val description: String
        get() = "Value of int 32 bit, inlined into instruction body"
    override val presentableName: String
        get() = "inlined i32 value"
    override val propertyName: String
        get() = "inlineValue"
}


object FunctionIndexValue : InstructionInlineValue() {
    override val size: Int
        get() = 2
    override val typeName: String
        get() = "Short"
    override val description: String
        get() = "Index of function in function table of compilation unit"
    override val presentableName: String
        get() = "function index"
    override val propertyName: String
        get() = "functionIndex"
}

object TypeIndexValue : InstructionInlineValue() {
    override val size: Int
        get() = 2
    override val typeName: String
        get() = "Short"
    override val description: String
        get() = "Index of type in type table"
    override val presentableName: String
        get() = "type index"
    override val propertyName: String
        get() = "typeIndex"
}

object VarTableIndexValue : InstructionInlineValue() {
    override val size: Int
        get() = 2
    override val typeName: String
        get() = "Short"
    override val description: String
        get() = "Index of global variable in global variable table of compilation unit"
    override val presentableName: String
        get() = "global variable index"
    override val propertyName: String
        get() = "globalVarIndex"
}

enum class LayoutType {
    Binary,
    Unary,
    IconstInplace,
    Bitcast,
    Call,
    Args,
    CallByPtr,
    FunctionPtr,
    Load,
    Store,
    Alloca,
    GetElementPtr,
    IconstI64,
    GetElementPtrVar,
    Noop,
}

class InstructionLayout(val values: List<InstructionInlineValue>, val type: LayoutType)

class InstructionDescription(
        val opcode: Byte,
        val name: List<String>, // lowercased
        val instructionLayout: InstructionLayout,
        val description: String
) {
    val titledName: String
        get() = name.joinToString("") { it.toTitle() }
    val snakeName: String
        get() = name.joinToString("_")
}

//fun binopLayout(): InstructionLayout {
//    return InstructionLayout(listOf(
//            OpcodeValue()
//    ))
//}


object Instructions {
    val instructions = mutableListOf<InstructionDescription>()
    var nextInstructionIndex = 0


    private fun instruction(name: List<String>, instructionLayout: InstructionLayout, description: String) {
        val opcode = nextInstructionIndex.toByte()
        nextInstructionIndex++
        instructions.add(InstructionDescription(opcode, name, instructionLayout, description))
    }

    private val binaryLayout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(3),
            OperandValue("Operand 1", "firstOperand"),
            OperandValue("Operand 2", "secondOperand")
    ), LayoutType.Binary)

    private val unaryLayout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(5),
            OperandValue("Operand", "operand")
    ), LayoutType.Binary)

    private val inplaceI32Layout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(3),
            InplaceI32Value
    ), LayoutType.IconstInplace)

    private val bitcastLayout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(7)
    ), LayoutType.Bitcast)

    private val callLayout = InstructionLayout(listOf(
            OpcodeValue,
            ArgsCountValue,
            FunctionIndexValue,
            OperandValue("first argument", "firstArg"),
            OperandValue("second argument", "secondArg")

    ), LayoutType.Call)

    private val callByPtrLayout = InstructionLayout(listOf(
            OpcodeValue,
            ArgsCountValue,
            OperandValue("pointer operand", "ptrOperand"),
            OperandValue("first argument", "firstArg"),
            OperandValue("second argument", "secondArg")

    ), LayoutType.CallByPtr)

    private val argsLayout = InstructionLayout(listOf(
            OpcodeValue,
            ArgsCountValue,
            OperandValue("first argument", "firstArg"),
            OperandValue("second argument", "secondArg"),
            OperandValue("third argument", "thirdArg")

    ), LayoutType.Args)

    private val functionPtrLayout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(1),
            FunctionIndexValue,
            EmptyValue(4)

    ), LayoutType.FunctionPtr)

    private val loadLayout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(1),
            OperandValue("pointer start", "startPtr"),
            EmptyValue(4)

    ), LayoutType.Load)

    private val storeLayout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(1),
            OperandValue("pointer start", "startPtr"),
            OperandValue("value", "value"),
            TypeIndexValue

    ), LayoutType.Store)

    private val allocaLayout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(7)

    ), LayoutType.Alloca)

    private val getElementPtrLayout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(1),
            OperandValue("element", "elementVar"),
            EmptyValue(4)

    ), LayoutType.GetElementPtr)

    private val getElementPtrVarLayout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(1),
            VarTableIndexValue,
            EmptyValue(4)

    ), LayoutType.GetElementPtrVar)

    private val iconstI64Layout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(1),
            VarTableIndexValue,
            EmptyValue(4)

    ), LayoutType.GetElementPtr)

    private val noopLayout = InstructionLayout(listOf(
            OpcodeValue,
            EmptyValue(7)

    ), LayoutType.Noop)

    private fun binaryInstruction(name: List<String>, description: String) = instruction(name, binaryLayout, description)

    private fun unaryInstruction(name: List<String>, description: String)= instruction(name, unaryLayout, description)

    init {
        binaryInstruction(listOf("add"), "Integer addition")
        binaryInstruction(listOf("sub"), "Integer subtraction")
        binaryInstruction(listOf("mul"), "Integer multiplication")
        binaryInstruction(listOf("div"), "Integer division. Undefined behavior deleting on zero")
        binaryInstruction(listOf("rem"), "Take reminder from division. Undefined behavior deleting on zero")
        unaryInstruction(listOf("inv"), "Bitwise complement of given value")
        instruction(listOf("iconst", "inplace", "i32"), inplaceI32Layout, "Loads i32 value, stored inside instruction")
        instruction(listOf("bitcast"), bitcastLayout, "Same semantics as reinterpret cast")
        instruction(listOf("call"), callLayout, "Call of function, which is known in this compilation unit")
        instruction(listOf("call", "by", "ptr"), callByPtrLayout, "Call of function, which address stored in variable")
        instruction(listOf("args"), argsLayout, "Additional args to the preceding call instruction")
        instruction(listOf("function", "ptr"), functionPtrLayout, "Obtains function pointer from some function")
        instruction(listOf("load"), storeLayout, "Store value from operand with a given type to memory")
        instruction(listOf("store"), storeLayout, "Load value from memory with a given type to operand")
        instruction(listOf("alloca"), allocaLayout, "Allocates enough memory on stack to store value of operand type")
        instruction(listOf("get", "element", "ptr"), getElementPtrLayout, "Returns pointer to a given operand")
        instruction(listOf("iconst", "i64"), iconstI64Layout, "Loads i64 from constant table of current compilation unit")
        instruction(listOf("get", "element", "ptr", "var"), getElementPtrVarLayout, "Returns pointer to global var from table")
        instruction(listOf("noop"), noopLayout, "Operation, that do nothing")

        for (instruction in instructions) {
            checkInstruction(instruction)
        }
    }

    private fun checkInstruction(instruction: InstructionDescription) {
        assert(instruction.name.isNotEmpty())
        assert(instruction.description.isNotBlank())
        assert(instruction.instructionLayout.values.sumBy { it.size } == 8)
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




