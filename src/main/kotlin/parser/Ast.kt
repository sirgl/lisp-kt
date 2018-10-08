package parser

import lexer.Token
import lexer.TokenType

abstract class AstNode(
        val textRange: TextRange,
        val type: SyntaxKind
) {
    abstract val children: Iterable<AstNode>

    override fun toString(): String = "[${textRange.startOffset}, ${textRange.endOffset})@${type.name}"
}

class FileNode(textRange: TextRange, override val children: List<AstNode>) : AstNode(textRange, SyntaxKind.File)

abstract class ListLikeNode(
        textRange: TextRange,
        syntaxKind: SyntaxKind,
        val lPar: AstNode,
        val rPar: AstNode,
        val innerNodes: List<AstNode>
) : AstNode(textRange, syntaxKind) {
    override val children: Iterable<AstNode>
        get() = object: Iterable<AstNode> {
            override fun iterator(): Iterator<AstNode> {
                return iterator {
                    yield(lPar)
                    for (innerNode in innerNodes) {
                        yield(innerNode)
                    }
                    yield(rPar)
                }
            }
        }
}

class ListNode(
        textRange: TextRange,
        syntaxKind: SyntaxKind,
        lPar: AstNode,
        rPar: AstNode,
        innerNodes: List<AstNode>
) : ListLikeNode(textRange, syntaxKind, lPar, rPar, innerNodes)

abstract class LeafNode(textRange: TextRange, type: SyntaxKind) : AstNode(textRange, type) {
    override val children: List<AstNode>
        get() = emptyList()
}

class ServiceNode(val range: TextRange) : LeafNode(range, SyntaxKind.Service)

abstract class ErrorNode(textRange: TextRange, val text: String) : LeafNode(textRange, SyntaxKind.Error) {
    override fun toString(): String = super.toString() + " $text"
}

class BadIntLiteral(
        textRange: TextRange,
        token: Token,
        text: String
) : ErrorNode(textRange, "Bad int: $text token: $token")

class UnexpectedTokenNode(
        textRange: TextRange,
        token: Token,
        tokenNameExpected: String
) : ErrorNode(textRange, "$tokenNameExpected expected, but $token token found")


class IdentifierNode(textRange: TextRange, val name: String) : LeafNode(textRange, SyntaxKind.Identifier) {
    override fun toString(): String = super.toString() + " $name"
}

abstract class LiteralNode(textRange: TextRange, type: SyntaxKind) : LeafNode(textRange, type)

class StringLiteralNode(textRange: TextRange, val value: String) : LiteralNode(textRange, SyntaxKind.StringLiteral) {
    override fun toString(): String = super.toString() + " $value"

}

class IntLiteralNode(textRange: TextRange, val value: Int) : LiteralNode(textRange, SyntaxKind.IntLiteral) {
    override fun toString(): String = super.toString() + " $value"
}

class CharLiteralNode(textRange: TextRange, val value: Char) : LiteralNode(textRange, SyntaxKind.IntLiteral) {
    override fun toString(): String = super.toString() + " $value"
}

class BoolLiteralNode(textRange: TextRange, val value: Boolean) : LiteralNode(textRange, SyntaxKind.BoolLiteral) {
    override fun toString(): String = super.toString() + " $value"
}

class SpecialFormNode(
        textRange: TextRange,
        syntaxKind: SyntaxKind,
        lPar: AstNode,
        rPar: AstNode,
        innerNodes: List<AstNode>,
        val specialFormType: SpecialFormType
) : ListLikeNode(textRange, syntaxKind, lPar, rPar, innerNodes)

//class DefineFormNode(textRange: TextRange, val name: AstNode, val value: AstNode) : AstNode(textRange, SyntaxKind.DefineForm) {
//    override val children: List<AstNode>
//        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
//
//}

enum class SyntaxKind {
    List,
    StringLiteral,
    IntLiteral,
    CharLiteral,
    BoolLiteral,
    Identifier,
    Error,
    File,
    IfForm,
    LetForm,
    DefineForm,
    Service
}

enum class SpecialFormType(val syntaxKind: SyntaxKind) {
    Let(SyntaxKind.LetForm),
    If(SyntaxKind.IfForm),
    Define(SyntaxKind.DefineForm)
}

fun TokenType.toSpecialFormType() : SpecialFormType? {
    return when (this) {
        TokenType.LetKw -> SpecialFormType.Let
        TokenType.DefineKw -> SpecialFormType.Define
        TokenType.IfKw -> SpecialFormType.If
        else -> null
    }
}