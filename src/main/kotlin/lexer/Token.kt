package lexer

class Token(
        val startOffset: Int,
        val text: String,
        val type: TokenType
) {
    override fun toString(): String = "$startOffset@${type.name}@\"$text\""

    fun withType(newType: TokenType): Token {
        return Token(startOffset, text, newType)
    }
}

enum class TokenType {
    Error,
    End,
    Lpar,
    Rpar,
    Identifier,
    TrueLiteral,
    FalseLiteral,
    Int,

    String,
    LetKw,
    DefineKw,
    IfKw,
    Whitespace,
    Comment,
}