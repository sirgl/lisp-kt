package parser

import lexer.Token
import util.Source

class Ast(
        val root: FileNode,
        val source: Source
)

sealed class AstNode(
        val syntaxKind: SyntaxKind,
        val textRange: TextRange
) {
    override fun toString(): String = "$textRange@$syntaxKind"

    abstract fun lispy() : String

    abstract val children: List<AstNode>
}

class LeafNode(val token: Token, syntaxKind: SyntaxKind) : AstNode(syntaxKind, token.textRange) {
    override fun lispy(): String = token.text

    override val children: List<AstNode>
        get() = emptyList()

    override fun toString(): String {
        return "${super.toString()} ${token.text}"
    }
}

class ListNode(override val children: List<AstNode>, textRange: TextRange) : AstNode(SyntaxKind.List, textRange) {
    override fun lispy(): String = children.joinToString(separator = " ", prefix = "(", postfix = ")") { it.lispy() }
}

class DataNode(val node: AstNode, textRange: TextRange) : AstNode(SyntaxKind.Data, textRange) {
    override fun lispy(): String {
        return "`${node.lispy()}"
    }

    override val children: List<AstNode> = listOf(node)
}

class FileNode(override val children: List<AstNode>, textRange: TextRange) : AstNode(SyntaxKind.File, textRange) {
    override fun lispy(): String {
        return children.joinToString("\n") { it.lispy() }
    }
}

enum class SyntaxKind {
    List,
    StringLiteral,
    IntLiteral,
    CharLiteral,
    BoolLiteral,
    Identifier,
    Data,
    File
}