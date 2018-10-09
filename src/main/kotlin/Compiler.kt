import backend.codegen.CodegenX64
import lexer.Lexer
import lexer.LexerIdentificationLayer
import parser.Parser

class Compiler(
        val lexer: Lexer,
        val parser: Parser,
        val lexerIdentificationLayer: LexerIdentificationLayer,
        val codegen: CodegenX64
) {

}