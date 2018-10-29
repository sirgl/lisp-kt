package lexer

class Token(
        val startOffset: Int,
        val text: String,
        val type: TokenType
) {
    override fun toString(): String = "$startOffset@${type.name}@\"$text\""
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
    Backtick,
    Whitespace,
    Comment,
}