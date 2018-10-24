package parser

import lexer.Token

sealed class AstNode(
        val syntaxKind: SyntaxKind,
        val textRange: TextRange
) {
    override fun toString(): String = "$textRange@$syntaxKind"
}

class LeafNode(val token: Token, syntaxKind: SyntaxKind) : AstNode(syntaxKind, token.textRange) {
    override fun toString(): String {
        return "${super.toString()} ${token.text}"
    }
}

class ListNode(val children: List<AstNode>, textRange: TextRange) : AstNode(SyntaxKind.List, textRange)


enum class SyntaxKind {
    List,
    StringLiteral,
    IntLiteral,
    CharLiteral,
    BoolLiteral,
    Identifier,
}