package lexer

class LexerIdentificationLayer(private val baseLexer: Lexer, val tokenRemapper: (Token) -> Token) : Lexer {
    override fun tokenize(text: CharSequence): List<Token> {
        return baseLexer.tokenize(text).map { tokenRemapper(it) }
    }
}

fun remapWithKeywords(token: Token): Token {
    if (token.type == TokenType.Identifier) {
        val text = token.text
        return when (text) {
            "define" -> token.withType(TokenType.DefineKw)
            "let" -> token.withType(TokenType.LetKw)
            "if" -> token.withType(TokenType.IfKw)
            else -> token
        }
    }
    return token
}