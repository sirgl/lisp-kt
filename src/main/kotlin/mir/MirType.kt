package mir

enum class MirType(val tagged: Boolean) {
    TaggedInt(true),
    TaggedBool(true),
    Int(false),
    Bool(false),
    CString(false),
    List(true),
    String(true)
}

sealed class MirInstrResultType {
    /**
     * The type can be inferred exactly
     */
    class Definite(val type: MirType) : MirInstrResultType() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Definite) return false

            if (type != other.type) return false

            return true
        }

        override fun hashCode(): Int {
            return type.hashCode()
        }
    }

    /**
     * Nothing about this type is known (e. g. it is parameter)
     */
    object Unknown : MirInstrResultType()

    /**
     * Instruction has no explicit return type (e. g. jump)
     */
    object NoType : MirInstrResultType()

    /**
     * Reports, that control can't reach end of this instruction (or it is unpredictable)
     * It means if impossible condition is happened, the type of the result may be error
     */
    class ErrorType(val errorText: String) : MirInstrResultType() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ErrorType) return false

            if (errorText != other.errorText) return false

            return true
        }

        override fun hashCode(): Int {
            return errorText.hashCode()
        }
    }
}