package frontend

import hir.HirLowering
import lexer.Lexer
import lexer.TokenValidator
import linting.Lint
import linting.Severity
import linting.Subsystem
import lir.LirCompilationUnit
import lir.World
import parser.ParseResult
import parser.Parser
import util.ResultWithLints
import util.Source

class LispFrontend(
        val sources: List<Source>,
        val stdLib: List<Source>,
        val lexer: Lexer,
        val parser: Parser,
        val tokenValidator: TokenValidator,
        val hirLowering: HirLowering,
        val lirLowering: LispLirLowering
) : Frontend {
    override fun run(): ResultWithLints<World> {
        val lints = mutableListOf<Lint>()
        val compilationUnits = mutableListOf<LirCompilationUnit>()
        for (source in sources) {
            val text = source.getInputStream().bufferedReader().readText()
            val tokens = lexer.tokenize(text)
            val lexerLints = tokenValidator.validate(tokens, source)
            lints.addAll(lexerLints)
            if (lexerLints.any { it.severity == Severity.Error }) {
                return ResultWithLints.Error(lexerLints)
            }
            val parseResult = parser.parse(tokens)
            val root = when (parseResult) {
                is ParseResult.Ok -> parseResult.node
                is ParseResult.Error -> {
                    val parseLint = Lint(parseResult.text, parseResult.textRange, Severity.Error, Subsystem.Parser, source)
                    return ResultWithLints.Error(lints + parseLint)
                }
            }

            // TODO macro expansion
            // TODO hir lowering


            val loweringResult = lirLowering.lower(root, source)
            lints.addAll(loweringResult.lints)
            when (loweringResult) {
                is ResultWithLints.Ok -> loweringResult.value
                is ResultWithLints.Error -> return ResultWithLints.Error(lints)
            }

//            compilationUnits.add(loweringResult)
        }
        return ResultWithLints.Ok(World(compilationUnits, lirLowering.typeStorage) , lints)
    }

}