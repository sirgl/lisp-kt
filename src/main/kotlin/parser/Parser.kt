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
    fun parse(tokens: List<Token>) : ParseResult {
        return try {
            ParseResult.Ok(ParseSession(tokens).parseFile())
        } catch (e: ParseException) {
            ParseResult.Error(e.text, e.range)
        }
    }
}

sealed class ParseResult {
    class Ok(val node: FileNode) : ParseResult() {
        override fun toString(): String = node.prettyPrint()
    }
    class Error(val text: String, val textRange: TextRange) : ParseResult() {
        override fun toString(): String = "ParseError $textRange : $text"
    }
}

private class ParseException(val text: String, val range: TextRange) :
        Exception("ParseError [${range.startOffset}, ${range.endOffset}): $text")

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

    fun parseFile() : FileNode {
        val children = mutableListOf<AstNode>()
        while (true) {
            if (current().type == TokenType.End) {
                val range = TextRange(children.first().textRange.startOffset, children.last().textRange.endOffset)
                return FileNode(children, range)
            }
            val node = parseExpr()
            children.add(node)
        }
    }

    private fun parseAtom() : AstNode {
        val token = advance()
        return when (token.type) {
            TokenType.Identifier -> LeafNode(token, SyntaxKind.Identifier)
            TokenType.TrueLiteral, TokenType.FalseLiteral -> LeafNode(token, SyntaxKind.BoolLiteral)
            TokenType.Int -> LeafNode(token, SyntaxKind.IntLiteral)
            TokenType.String -> LeafNode(token, SyntaxKind.StringLiteral)
            else -> throw ParseException("Atom node expected", token.textRange)
        }
    }

    private fun parseList() : AstNode {
        val first = advance()
        val innerNodes = mutableListOf<AstNode>()
        while (current().type != TokenType.Rpar) {
            innerNodes.add(parseExpr())
        }
        val last = expect(TokenType.Rpar) ?: throw ParseException("')' expected", current().textRange)
        return ListNode(innerNodes, TextRange(first, last))
    }

    private fun parseExpr() : AstNode {
        val currentType = current().type
        return when (currentType) {
            TokenType.Backtick -> parseDataNode()
            TokenType.Lpar -> parseList()
            else -> parseAtom()
        }
    }

    private fun parseDataNode(): AstNode {
        val token = advance()
        val expr = parseExpr()
        return DataNode(expr, TextRange(token.startOffset, expr.textRange.endOffset))
    }
}