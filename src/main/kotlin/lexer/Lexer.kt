package lexer

interface Lexer {
    fun tokenize(text: CharSequence): List<Token>
}

class LexerImpl : Lexer {
    override fun tokenize(text: CharSequence): List<Token> {
        val session = LexerSession(text)
        val tokens = mutableListOf<Token>()
        while (true) {
            val nextToken = session.nextToken()
            tokens.add(nextToken)
            if (nextToken.type == TokenType.End) break
        }
        return tokens
    }
}

private class LexerSession(
        private val text: CharSequence,
        private var offset: Int = 0,
        private val length: Int = text.length
) {
    private var errorStartOffset = 0
    // field set when we finish skippable token. But we do not return it and information of its length disappear
    private var errorEndOffset = -1
    private var isInErrorMode = false
    private var nextToken: Token? = null

    fun nextToken(): Token {
        // On the previous call of nextToken error was returned
        if (nextToken != null) {
            val token = nextToken!!
            nextToken = null
            if (!isSkippable(token)) {
                return token
            } else {
                handleSkippableToken(token)
            }
        }
        // Loop is for handling error as maximal sequence of chars not fitting to any rule
        while (true) {
            val normalToken = nextNormalToken()
            if (normalToken != null) {
                shiftByLen(normalToken)
                if (isSkippable(normalToken)) {
                    if (isInErrorMode) {
                        handleSkippableToken(normalToken)
                    }
                    continue
                }
                return getTokenToReturn(normalToken)
            }

            // No rule match character

            if (!isInErrorMode) {
                isInErrorMode = true
                errorStartOffset = offset
            }
            offset++
        }
    }

    private fun isSkippable(token: Token): Boolean {
        return token.type == TokenType.Comment || token.type == TokenType.Whitespace
    }

    private fun nextNormalToken(): Token? {
        if (endReached()) return createEnd()
        return parseWhitespace()
                ?: parseComment()
                ?: parseInt()
                ?: parseLpar()
                ?: parseRpar()
                ?: parseBool()
                ?: parseIdentifier()
    }

    private fun parseInt(): Token? = parseByAllCharsRule(TokenType.Int) { it.isDigit() }

    private fun parseIdentifier(): Token? =
            parseByAllCharsRule(TokenType.Identifier) { it in 'a'..'z' || it in 'A'..'Z' }

    private inline fun parseByAllCharsRule(type: TokenType, isGoodChar: (Char) -> Boolean): Token? {
        val startOffset = offset
        var endOffset = offset
        while (!endReached(endOffset) && isGoodChar(at(endOffset))) {
            endOffset++
        }
        return createTokenIfPresent(startOffset, endOffset, type)
    }

    private fun parseLpar(): Token? = parseBySingleLetter('(', TokenType.Lpar)

    private fun parseRpar(): Token? = parseBySingleLetter(')', TokenType.Rpar)

    private fun parseBySingleLetter(char: Char, type: TokenType): Token? {
        if (current() == char) {
            return createTokenIfPresent(offset, offset + 1, type)
        }
        return null
    }

    private fun parseBool(): Token? {
        if (current() != '#') return null
        val secondOffset = offset + 1
        if (endReached(secondOffset)) return null
        val second = at(secondOffset)
        if (second == 't') return createTokenIfPresent(offset, secondOffset + 1, TokenType.TrueLiteral)
        if (second == 'f') return createTokenIfPresent(offset, secondOffset + 1, TokenType.FalseLiteral)
        return null
    }

    private fun parseWhitespace(): Token? = parseByAllCharsRule(TokenType.Whitespace) { it.isWhitespace() }

    // comment is #| |#, also it can be nested
    private fun parseComment(): Token? {
//        var unpairedLeft = 0
//        while ()
        return null
    }

    // Service functions

    private fun shiftByLen(token: Token) {
        offset += token.text.length
    }


    private fun createEnd(): Token {
        return Token(length, "", TokenType.End)
    }


    private fun createTokenIfPresent(startOffset: Int, endOffset: Int, type: TokenType): Token? {
        if (endOffset == startOffset) return null
        return Token(startOffset, text.substring(startOffset, endOffset), type)
    }

    private fun getTokenToReturn(token: Token): Token {
        return when {
            isInErrorMode -> {
                isInErrorMode = false
                nextToken = token
                val oldErrorStartOffset = errorStartOffset
                errorStartOffset = -1
                val errorEndOffset = if (errorEndOffset == -1) {
                    offset - token.text.length
                } else {
                    errorEndOffset
                }
                this.errorEndOffset = -1
                Token(oldErrorStartOffset, text.substring(oldErrorStartOffset, errorEndOffset), TokenType.Error)
            }
            else -> token
        }
    }

    fun handleSkippableToken(token: Token) {
        errorEndOffset = offset - token.text.length
    }

    private fun endReached(currentOffset: Int = offset): Boolean {
        return currentOffset >= length
    }

    private fun current(): Char = text[offset]
    private fun at(index: Int): Char = text[index]
}

