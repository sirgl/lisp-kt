import deps.DependencyValidator
import frontend.QueryDrivenLispFrontend
import hir.HirImport
import hir.HirLowering
import hir.HirValidator
import lexer.LexerImpl
import lexer.TokenValidator
import lir.LirLowering
import macro.MacroExpander
import mir.MirLowering
import parser.Ast
import parser.ParseResult
import parser.Parser
import util.InMemorySource

class InMemoryFileInfo(val text: String, val name: String)

infix fun String.withText(text: String): InMemoryFileInfo = InMemoryFileInfo(text, this)


open class MultifileAstBasedTest {
    private val lexer = LexerImpl()
    private val parser = Parser()

    fun buildAsts(files: List<InMemoryFileInfo>): List<Ast> {
        return files.map {
            val tokens = lexer.tokenize(it.text)
            Ast(when (
                val res = parser.parse(tokens)) {
                is ParseResult.Ok -> res.node
                is ParseResult.Error -> throw IllegalStateException(res.text)
            }, InMemorySource(it.text, it.name))
        }
    }
}

abstract class FrontendTest(val implicitImports: List<HirImport>) {
    val frontend = QueryDrivenLispFrontend(
            LexerImpl(),
            Parser(),
            TokenValidator(),
            HirLowering(implicitImports),
            LirLowering(),
            DependencyValidator(),
            HirValidator(),
            MacroExpander(),
            MirLowering()
    )
}