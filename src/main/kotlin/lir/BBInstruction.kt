package lir


inline class Operand(val storage: Short)

inline class BBInstruction(val storage: Long) {
    val opcode: Byte
        get() = (storage shr 56).toByte()
    override fun toString(): String {
        return Instructions.toStrings[opcode.toInt()](this)
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