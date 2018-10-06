package parser

import lexer.Token
import util.LongStorage

// TODO inline
class TextRange(private val storage: LongStorage) {
    constructor(
            startOffset: Int,
            endOffset: Int
    ) : this(LongStorage(startOffset, endOffset))

    constructor(start: Token, end: Token) : this(start.startOffset, end.startOffset + end.text.length)

    constructor(token: Token) : this(token, token)

    val startOffset: Int
        get() = storage.first
    val endOffset: Int
        get() = storage.second
}

val Token.textRange: TextRange
    get() = TextRange(this)