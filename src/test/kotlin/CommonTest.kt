import parser.Ast
import parser.ParseResult
import util.InMemorySource
import lexer.LexerImpl
import parser.Parser

class InMemoryFileInfo(val text: String, val name: String)

infix fun String.withText(text: String): InMemoryFileInfo = InMemoryFileInfo(text, this)


open class MultifileAstBasedTest {
    private val lexer = LexerImpl()
    private val parser = Parser()

    fun buildAsts(files: List<InMemoryFileInfo>): List<Ast> {
        return files.map {
            Ast(when (
                val res = parser.parse(lexer.tokenize(it.text))) {
                is ParseResult.Ok -> res.node
                else -> throw IllegalStateException()
            }, InMemorySource(it.text, it.name))
        }
    }
}