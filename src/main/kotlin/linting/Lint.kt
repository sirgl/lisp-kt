package linting

import parser.TextRange
import util.Source

class Lint(
        val text: String,
        val textRange: TextRange,
        val severity: Severity,
        val subsystem: Subsystem,
        val source: Source
) {
    override fun toString(): String {
        return "${source.path} : $severity in $subsystem $textRange : $text"
    }
}

enum class Severity {
    Error,
    Warning,
    Notice
}

enum class Subsystem {
    Lexer,
    Parser,
    MacroExpander,
    LoweringToHir,
    LoweringToLir,
    Codegen,
    Validation
}