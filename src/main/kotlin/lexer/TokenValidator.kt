package lexer

import linting.LintSink
import linting.Lint
import linting.Severity
import linting.Subsystem
import parser.textRange
import util.Source

class TokenValidator {
    fun validate(tokens: List<Token>, source: Source, lintSink: LintSink) {
        for (token in tokens
                .filter { it.type == TokenType.Int && it.text.toIntOrNull() == null }) {
            lintSink.addLint(Lint("Int literal is too big", token.textRange, Severity.Error, Subsystem.Lexer, source))
        }
    }
}