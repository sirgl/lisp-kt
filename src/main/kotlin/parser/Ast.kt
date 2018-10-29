package parser

import lexer.Token

sealed class AstNode(
        val syntaxKind: SyntaxKind,
        val textRange: TextRange
) {
    override fun toString(): String = "$textRange@$syntaxKind"

    abstract val children: List<AstNode>
}

class LeafNode(val token: Token, syntaxKind: SyntaxKind) : AstNode(syntaxKind, token.textRange) {
    override val children: List<AstNode>
        get() = emptyList()

    override fun toString(): String {
        return "${super.toString()} ${token.text}"
    }
}

class ListNode(override val children: List<AstNode>, textRange: TextRange) : AstNode(SyntaxKind.List, textRange)

class DataNode(val node: AstNode, textRange: TextRange) : AstNode(SyntaxKind.Data, textRange) {
    override val children: List<AstNode> = listOf(node)
}

class FileNode(override val children: List<AstNode>, textRange: TextRange) : AstNode(SyntaxKind.File, textRange)

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