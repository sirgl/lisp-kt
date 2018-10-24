import backend.codegen.CodegenX64
import lexer.Lexer
import parser.Parser

class Compiler(
        val lexer: Lexer,
        val parser: Parser,
        val codegen: CodegenX64
) {

}