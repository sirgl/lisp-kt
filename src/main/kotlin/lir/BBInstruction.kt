package lir

import util.LongStorage


inline class Opcode(val storage: Byte) {
    override fun toString(): String = Opcodes[storage].name

    val description: OpDescription
        get() = Opcodes[storage]
}

inline class Operand(val storage: Short)

inline class BBInstruction(val storage: LongStorage) {
    constructor(opcode: Opcode, operand: Operand) : this(LongStorage(opcode.storage.toShort(), operand.storage, 0, 0))
    constructor(opcode: Opcode, first: Operand, second: Operand) : this(LongStorage(opcode.storage.toShort(), first.storage, second.storage, 0))

    val opcode: Opcode
        get() = Opcode(storage.ab.toByte())

    val firstOperand: Operand
        get() = Operand(storage.cd)

    val secondOperand: Operand
        get() = Operand(storage.ef)

    override fun toString(): String {
        val description = opcode.description
        return buildString {
            append(opcode)
            append(" TODO extract from description")
        }
    }
}

/**
 * @param index index from
 */
inline class InstructionIndex(val index: Short) {
    fun getVariableIndex(parameterCount: Int): Int = index + parameterCount

    override fun toString(): String {
        return index.toString()
    }
}