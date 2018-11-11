package frontend

import analysis.AppendingSink
import deps.DependencyGraphBuilder
import deps.DependencyVerifier
import hir.HirLowering
import lexer.Lexer
import lexer.TokenValidator
import linting.Lint
import linting.Severity
import linting.Subsystem
import lir.LirCompilationUnit
import lir.World
import macro.MacroExpander
import parser.*
import util.ResultWithLints
import util.Source

class LispFrontend(
        val sources: List<Source>,
        val stdLib: List<Source>,
        val lexer: Lexer,
        val parser: Parser,
        val tokenValidator: TokenValidator,
        val hirLowering: HirLowering,
        val lirLowering: LispLirLowering,
        val targetIndex: Int,
        val dependencyVerifier: DependencyVerifier,
        val macroExpander: MacroExpander
) : Frontend {
    override fun run(): ResultWithLints<World> {
        val lints = mutableListOf<Lint>()
        val compilationUnits = mutableListOf<LirCompilationUnit>()
        val asts = mutableListOf<Ast>()
        val lintSink = AppendingSink(lints)
        val mergedSources = sources + stdLib
        for (source in mergedSources) {
            val text = source.getInputStream().bufferedReader().readText()
            val tokens = lexer.tokenize(text)
            tokenValidator.validate(tokens, source, lintSink)
            if (lints.any { it.severity == Severity.Error }) {
                return ResultWithLints.Error(lints)
            }
            val parseResult = parser.parse(tokens)
            val root = when (parseResult) {
                is ParseResult.Ok -> parseResult.node
                is ParseResult.Error -> {
                    val parseLint = Lint(parseResult.text, parseResult.textRange, Severity.Error, Subsystem.Parser, source)
                    return ResultWithLints.Error(lints + parseLint)
                }
            }
            asts.add(Ast(root, source))
        }
        val dependencyGraphBuilder = DependencyGraphBuilder(asts)
        val dependencyGraph= dependencyGraphBuilder.build().drainTo(lints) ?: return ResultWithLints.Error(lints)
        dependencyVerifier.verifyDependencies(dependencyGraph, lintSink)
        if (lints.any { it.severity == Severity.Error }) {
            return ResultWithLints.Error(lints)
        }
        val target = dependencyGraph[targetIndex]
        val finalAsts = macroExpander.expand(asts, target).drainTo(lints) ?: return ResultWithLints.Error(lints)



        return ResultWithLints.Ok(World(compilationUnits, lirLowering.typeStorage) , lints)
    }

}