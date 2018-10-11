package lir

import util.LongStorage


inline class Opcode(val storage: Byte)

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