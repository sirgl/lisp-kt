package analysis

import parser.AstNode

/**
 * Class representing all the info about particular ast node (not recursive, no info about children, that has complex structure)
 */
sealed class NodeInfo

sealed class FuncLikeInfo : NodeInfo() {
    abstract val name: String
    abstract val parameters: List<ParameterInfo>
}

sealed class FuncLikeDefinitionInfo : FuncLikeInfo() {
        abstract val body: List<AstNode>

}

class ParameterInfo(val name: String, val isVararg: Boolean)

class DefnNodeInfo(
        override val name: String,
        override val parameters: List<ParameterInfo>,
        override val body: List<AstNode>
) : FuncLikeDefinitionInfo()

class MacroNodeInfo(
        override val name: String,
        override val parameters: List<ParameterInfo>,
        override val body: List<AstNode>
) : FuncLikeDefinitionInfo()

class MacroAsmNodeInfo(
        val name: String,
        val body: AstNode
) : NodeInfo()

class EmitNodeInfo(
        val body: String
) : NodeInfo()

class ModuleNodeInfo(
        val name: String
) : NodeInfo()

class ImportNodeInfo(
        val name: String
) : NodeInfo()

class LetDecl(
        val name: String,
        val initializer: AstNode
)

class LetNodeInfo(
        val declarations: List<LetDecl>,
        val body: List<AstNode>
) : NodeInfo()

class IfNodeInfo(
        val condition: AstNode,
        val thenBranch: AstNode,
        val elseBranch: AstNode?
) : NodeInfo()

class WhileNodeInfo(
        val condition: AstNode,
        val body: List<AstNode>
) : NodeInfo()

class SetNodeInfo(
        val name: String,
        val newValue: AstNode
) : NodeInfo()

class NativeFunctionDeclarationInfo(
        val nameInProgram: String,
        val nameInRuntime: String,
        override val parameters: List<ParameterInfo>
) : FuncLikeInfo() {
        override val name: String
                get() = nameInProgram
}

class MacroAsmExpandedInfo(
        val name: String,
        val asmText: String
) : NodeInfo()