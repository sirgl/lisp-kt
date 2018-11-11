package analysis

import parser.AstNode
import parser.ListNode

/**
 * Class representing all the info about particular ast node (not recursive, no info about children, that has complex structure)
 */
sealed class NodeInfo

sealed class FuncLikeInfo : NodeInfo() {
    abstract val name: String
    abstract val parameters: List<String>
    abstract val body: List<AstNode>
}

class DefnNodeInfo(
        override val name: String,
        override val parameters: List<String>,
        override val body: List<AstNode>
) : FuncLikeInfo()

class MacroNodeInfo(
        override val name: String,
        override val parameters: List<String>,
        override val body: List<AstNode>
) : FuncLikeInfo()

class ModuleNodeInfo(
        val name: String
) : NodeInfo()

class ImportNodeInfo(
        val name: String
) : NodeInfo()