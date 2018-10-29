package lexer

import linting.Lint
import linting.Severity
import linting.Subsystem
import parser.textRange
import util.Source

class TokenValidator {
    fun validate(tokens: List<Token>, source: Source): List<Lint> {
        return tokens
                .filter { it.type == TokenType.Int && it.text.toIntOrNull() == null }
                .map { Lint("Int literal is too big", it.textRange, Severity.Error, Subsystem.Lexer, source) }
    }
}