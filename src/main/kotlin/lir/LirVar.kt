package lir

import lir.types.LirType

enum class LirVariableVisibility {
    Private,
    Public
}

// indexation is implicit?
class LirVar(
        val name: String?,
        val isConstant: Boolean,
        val type: LirType,
        val visibility: LirVariableVisibility,
        val initializer: LirVarInitializer? // may be extern
) {
    override fun toString(): String {
        return buildString {
            append("${visibility.toString().toLowerCase()} global ")
            if (isConstant) {
                append("const ")
            }
            append("${name ?: "<anon>"}: $type")
            initializer?.let { append(" = $it") }
        }
    }
}

inline class VariableIndex(val storage: Short)

sealed class LirVarInitializer

sealed class MemoryInitializer : LirVarInitializer() {
    // TODO members?
}

object ZeroInit : MemoryInitializer() {
    override fun toString(): String {
        return "zeroinit"
    }
}

class LongConstantInit(val value: Long) : MemoryInitializer() {
    override fun toString(): String = value.toString()
}

class AddressInit(val index: VariableIndex) {

}