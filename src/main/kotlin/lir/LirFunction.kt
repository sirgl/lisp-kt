package lir

import backend.codegen.CallingConvention
import lir.types.LirType
import lir.types.TypeStorage
import java.util.*

/**
 * @param callingConvention calling convention, that must be used when invoking this function.
 * If null, default value for given platform will be used
 */
class LirFunction(
        val name: String?,
        val blocks: List<BasicBlock>,
        val functionId: FunctionId,
        val returnType: LirType,
        val visibility: LirFunctionVisibility = LirFunctionVisibility.Private,
        val callingConvention: CallingConvention? = null
) {
    override fun toString(): String {
        return buildString {
            appendFunctionDescription(callingConvention)
            append(blocks.joinToString("\n") { it.toString() })
        }
    }

    private fun StringBuilder.appendFunctionDescription(callingConvention: CallingConvention?) {
        append("$visibility fun #$functionId ${name ?: "<anon>"} -> ${returnType.typeDescriptor.presentableName}\n")
        callingConvention?.let { append("\tcalling convention: ${callingConvention.name}\n") }
    }

    fun pretty(typeStorage: TypeStorage): String {
        return buildString {
            appendFunctionDescription(callingConvention)
            append(blocks.joinToString("\n") { it.pretty(typeStorage) })
        }
    }

//    private fun blocks() : List<BasicBlock> {
//        if (startBlock == null) return emptyList()
//        val basicBlocks = hashSetOf<BasicBlock>()
//        val blocks = mutableListOf<BasicBlock>()
//        val toHandle = ArrayDeque<BasicBlock>()
//        toHandle.add(startBlock)
//        while (toHandle.isNotEmpty()) {
//            val block = toHandle.peek()
//            basicBlocks.add(block)
//            val tailInstruction = block.tailInstruction
//            when (tailInstruction) {
//                is GotoInstruction -> tailInstruction.blockId
//            }
//        }
//    }
}

inline class FunctionId(val id: Int) {
    override fun toString(): String = id.toString()
}

enum class LirFunctionVisibility(private val pretty: String) {
    Public("public"),
    Private("private");

    override fun toString(): String = pretty
}