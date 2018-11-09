package analysis

import parser.AstNode
import parser.ListNode

/**
 * Class representing all the info about particular ast node (not recursive, no info about children, that has complex structure)
 */
sealed class NodeInfo

class DefnNodeInfo(
        val name: String,
        val parameters: List<String>,
        val body: List<AstNode>
) : NodeInfo()

class ModuleNodeInfo(
        val name: String
) : NodeInfo()

class ImportNodeInfo(
        val name: String
) : NodeInfo()