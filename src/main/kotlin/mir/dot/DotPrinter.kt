package mir.dot

import mir.*

fun getBBGraph(function: MirFunctionDefinition) : String {
    return buildString {
        append("digraph ${function.name} {\n")
        append("entry -> b0\n")
        for (block in function.blocks) {
            append("b${block.index} [shape=box, label=\"$block\"]\n")
            val last = block.instructions.last() as? MirTailInstruction ?: continue
            when (last) {
                is MirGotoInstruction -> {
                    if (last.basicBlockIndex.toInt() != -1) {
                        append("b${block.index} -> b${last.basicBlockIndex}\n")
                    }
                }
                is MirCondJumpInstruction -> {
                    if (last.thenBlockIndex.toInt() != -1) {
                        append("b${block.index} -> b${last.thenBlockIndex}\n")
                    }
                    if (last.elseBlockIndex.toInt() != -1) {
                        append("b${block.index} -> b${last.elseBlockIndex}\n")
                    }
                }
                is MirReturnInstruction -> {
                    append("b${block.index} -> exit\n")
                }
            }

        }
        append("}")
    }
}