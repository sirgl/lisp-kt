package lir

import backend.codegen.CallingConvention

/**
 * @param callingConvention calling convention, that must be used when invoking this function.
 * If null, default value for given platform will be used
 */
class LirFunction(
        val name: String,
        val basicBlock: BasicBlock,
        val callingConvention: CallingConvention? = null,
        val functionId: FunctionId
) {
    override fun toString(): String {
        TODO("print llir")
    }
}

inline class FunctionId(val id: Int) {
    override fun toString(): String = id.toString()
}