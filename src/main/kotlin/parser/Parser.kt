package parser

import lexer.Token
import lexer.TokenType

/**
 * Grammar:
 * file -> expr*
 * expr -> atom | list
 * list -> ( expr* )
 * atom -> boolLiteral | strLiteral | charLiteral | intLiteral
 * boolLiteral -> true | false
 */
class Parser {
    fun parse(tokens: List<Token>) : AstNode {
        return ParseSession(tokens).parseFile()
    }
}

internal class ParseSession(private val tokens: List<Token>, private var index: Int = 0) {
    /**
     * if type of current token is same, it advances, otherwise it return null
     */
    private fun expect(type: TokenType): Token? {
        return when (type) {
            current().type -> advance()
            else -> null
        }
    }

    /**
     * Advances token stream and returns previous token value
     */
    private fun advance() : Token {
        val currentIndex = index
        index++
        return tokens[currentIndex]
    }

    private fun current() : Token {
        return tokens[index]
    }

    fun parseFile() : AstNode {
        val children = mutableListOf<AstNode>()
        while (true) {
            if (current().type == TokenType.End) {
                val range = TextRange(children.first().textRange.startOffset, children.last().textRange.endOffset)
                return FileNode(range, children)
            }
            val node = parseExpr()
            children.add(node)
        }
    }

    fun parseAtom() : AstNode {
        val token = advance()
        val range = TextRange(token)
        return when (token.type) {
            TokenType.Identifier -> IdentifierNode(range, token.text)
            TokenType.TrueLiteral -> BoolLiteralNode(range, true)
            TokenType.FalseLiteral -> BoolLiteralNode(range, false)
            TokenType.Int -> {
                val value = token.text.toIntOrNull()
                if (value == null) {
                    BadIntLiteral(range, token, "Number doesn't fit into integer")
                } else {
                    IntLiteralNode(range, value)
                }
            }
            TokenType.String -> StringLiteralNode(range, token.text)
            else -> UnexpectedTokenNode(range, token, "Atom node expected")
        }
    }

    private fun parseList() : AstNode {
        val (lParNode, lParToken) = parseServiceNode(TokenType.Lpar, "lPar")
        val first = current()
        val firstNode = when (first.type) {
            TokenType.LetKw -> ServiceNode(first.textRange)
            TokenType.DefineKw -> ServiceNode(first.textRange)
            TokenType.IfKw -> ServiceNode(first.textRange)
            else -> parseExpr()
        }
        val innerNodes = mutableListOf<AstNode>()
        // TODO
        innerNodes.add(firstNode)
        while (current().type != TokenType.Rpar) {
            innerNodes.add(parseExpr())
        }
        val (rParNode, rParToken) = parseServiceNode(TokenType.Rpar, "rPar")
        // TODO parse special forms
        return ListNode(TextRange(lParToken, rParToken), SyntaxKind.List, lParNode, rParNode, innerNodes)
    }

    private fun parseServiceNode(tokenType: TokenType, expectedTokenTypeMessage: String): Pair<LeafNode, Token> {
        val lPar = expect(tokenType)
        return if (lPar == null) {
            val errorNode = advance()
            UnexpectedTokenNode(errorNode.textRange, errorNode, expectedTokenTypeMessage) to errorNode
        } else {
            ServiceNode(lPar.textRange) to lPar
        }
    }

    private fun parseExpr() : AstNode {
        return if (current().type == TokenType.Lpar) {
            parseList()
        } else {
            parseAtom()
        }
    }
}