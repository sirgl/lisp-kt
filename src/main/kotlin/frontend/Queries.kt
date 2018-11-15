@file:Suppress("UNCHECKED_CAST")

package frontend

import deps.*
import hir.HirFile
import hir.HirLowering
import lexer.Lexer
import lexer.LexerImpl
import lexer.Token
import lexer.TokenValidator
import linting.*
import macro.MacroExpander
import mir.MirFile
import mir.MirLowering
import mir.dot.getBBGraph
import parser.Ast
import parser.ParseResult
import parser.Parser
import query.*
import util.InMemorySource
import util.ResultWithLints
import util.Source
import java.io.File

const val sourcesKey = "sources"
val inputSourceDescriptor = SingleValueDescriptor<List<Source>>(sourcesKey)
const val stdlibKey = "stdlib"
val stdlibSourceDescriptor = SingleValueDescriptor<List<Source>>(stdlibKey)
const val mergedKey = "merged"
val mergedSourceDescriptor = SingleValueDescriptor<List<Source>>(mergedKey)
const val configKey = "config"
val configDescriptor = SingleValueDescriptor<CompilerConfig>(configKey)
const val tokensKey = "tokens"
val tokensDescriptor = SingleValueDescriptor<ResultWithLints<TokenizedSources>>(tokensKey)
const val astsKey = "asts"
val astsDescriptor = SingleValueDescriptor<ResultWithLints<List<Ast>>>(astsKey)
const val initialDependenciesKey = "initial deps"
val initialDependenciesDescriptor = SingleValueDescriptor<ResultWithLints<List<DependencyEntry>>>(initialDependenciesKey)
const val macroExpandedAstKey = "macro expanded ast"
val macroExpandedAstDescriptor = SingleValueDescriptor<ResultWithLints<List<Ast>>>(macroExpandedAstKey)
const val macroExpandedDependenciesKey = "macro expanded dependencies"
val macroExpandedDependenciesDescriptor = SingleValueDescriptor<ResultWithLints<List<DependencyEntry>>>(macroExpandedDependenciesKey)
const val hirKey = "hir"
val hirDescriptor = SingleValueDescriptor<ResultWithLints<List<HirFile>>>(hirKey)
const val mirKey = "mir"
val mirDescriptor = SingleValueDescriptor<ResultWithLints<List<MirFile>>>(mirKey)

class MergedQueryInput(
        val inputs: List<Source>,
        val stdlib: List<Source>
)

class MergedQuery : Query<MergedQueryInput, List<Source>> {
    override fun doQuery(input: MergedQueryInput): List<Source> = input.inputs + input.stdlib

    override val outputDescriptor = mergedSourceDescriptor
    override val inputDescriptor =
            MultiValueDescriptor(listOf(stdlibKey, sourcesKey)) { MergedQueryInput(it[0] as List<Source>, it[1] as List<Source>) }
}

class TokenizedSources(val sourcesWithTokens: List<Pair<Source, List<Token>>>)

class TokensQuery(
        val lexer: Lexer,
        val tokenValidator: TokenValidator
) : Query<List<Source>, ResultWithLints<TokenizedSources>> {
    override val outputDescriptor = tokensDescriptor
    override val inputDescriptor = mergedSourceDescriptor

    override fun doQuery(input: List<Source>): ResultWithLints<TokenizedSources> {
        val lintSink = CollectingSink()
        val sourceWithTokens = input.map { source ->
            val tokens = lexer.tokenize(source.getInputStream().bufferedReader().readText())
            tokenValidator.validate(tokens, source, lintSink)
            source to tokens
        }
        val lints = lintSink.lints
        if (lints.any { it.severity == Severity.Error }) {
            return ResultWithLints.Error(lints)
        }
        return ResultWithLints.Ok(TokenizedSources(sourceWithTokens), lints)
    }
}

class AstBuildingQuery(
        val parser: Parser
) : Query<ResultWithLints<TokenizedSources>, ResultWithLints<List<Ast>>> {
    override fun doQuery(input: ResultWithLints<TokenizedSources>): ResultWithLints<List<Ast>> {
        if (input.isError()) return ResultWithLints.Error(input.lints)
        input as ResultWithLints.Ok
        val value = input.value
        val lints = input.lints.toMutableList()
        return ResultWithLints.Ok(value.sourcesWithTokens.map {
            val tokens = it.second
            val source = it.first
            val parseResult = parser.parse(tokens)
            val fileNode = when (parseResult) {
                is ParseResult.Ok -> parseResult.node
                is ParseResult.Error -> {
                    val parseLint = Lint(parseResult.text, parseResult.textRange, Severity.Error, Subsystem.Parser, source)
                    return ResultWithLints.Error(lints + parseLint)
                }
            }
            Ast(fileNode, source)
        })
    }

    override val outputDescriptor = astsDescriptor
    override val inputDescriptor = tokensDescriptor
}

class InitialDependenciesQuery(
        val dependencyValidator: DependencyValidator
) : Query<ResultWithLints<List<Ast>>, ResultWithLints<List<DependencyEntry>>> {
    override fun doQuery(input: ResultWithLints<List<Ast>>): ResultWithLints<List<DependencyEntry>> {
        if (input is ResultWithLints.Error) return ResultWithLints.Error(input.lints)
        val lints = input.lints.toMutableList()
        input as ResultWithLints.Ok
        val asts = input.value
        val dependencyGraphBuilder = DependencyGraphBuilder(asts)
        val dependencies = dependencyGraphBuilder.build().drainTo(lints) ?: return ResultWithLints.Error(lints)
        dependencyValidator.validateDependencies(dependencies, AppendingSink(lints))
        if (lints.any { it.severity == Severity.Error }) return ResultWithLints.Error(lints)
        return ResultWithLints.Ok(dependencies, lints)
    }

    override val outputDescriptor = initialDependenciesDescriptor
    override val inputDescriptor = astsDescriptor
}


class MacroExpansionInput(
        val config: CompilerConfig,
        val initialAsts: ResultWithLints<List<Ast>>,
        val dependecies: ResultWithLints<List<DependencyEntry>>
)

class MacroExpansionQuery(
        val macroExpander: MacroExpander
) : Query<MacroExpansionInput, ResultWithLints<List<Ast>>> {
    override fun doQuery(input: MacroExpansionInput): ResultWithLints<List<Ast>> {
        val initialAsts = input.initialAsts
        if (initialAsts is ResultWithLints.Error) return ResultWithLints.Error(initialAsts.lints)
        val dependecies = input.dependecies
        if (dependecies is ResultWithLints.Error) return ResultWithLints.Error(dependecies.lints)
        initialAsts as ResultWithLints.Ok
        dependecies as ResultWithLints.Ok
        val asts = initialAsts.value
        val targetSourceIndex = input.config.targetSourceIndex
        val deps = dependecies.value
        val targetSourceEntry = deps[targetSourceIndex]
        return macroExpander.expand(asts, targetSourceEntry)
    }

    override val outputDescriptor = macroExpandedAstDescriptor
    override val inputDescriptor =
            MultiValueDescriptor(listOf(configKey, astsKey, initialDependenciesKey)) {
                MacroExpansionInput(
                        it[0] as CompilerConfig,
                        it[1] as ResultWithLints<List<Ast>>,
                        it[2] as ResultWithLints<List<DependencyEntry>>
                )
            }
}

class DependencyInput(
        val compilerConfig: CompilerConfig,
        val unwrappedAsts: ResultWithLints<List<Ast>>,
        val initialDependencies: ResultWithLints<List<DependencyEntry>>
)

class DependenciesRemappingQuery : Query<DependencyInput, ResultWithLints<List<DependencyEntry>>> {
    override fun doQuery(input: DependencyInput): ResultWithLints<List<DependencyEntry>> {
        val unwrappedAsts = input.unwrappedAsts
        if (unwrappedAsts is ResultWithLints.Error) return ResultWithLints.Error(unwrappedAsts.lints)
        val initialDependencies = input.initialDependencies
        if (initialDependencies is ResultWithLints.Error) return ResultWithLints.Error(initialDependencies.lints)
        unwrappedAsts as ResultWithLints.Ok
        initialDependencies as ResultWithLints.Ok
        val config = input.compilerConfig
        val initialDeps = initialDependencies.value
        val entry = initialDeps[config.targetSourceIndex]
        return ResultWithLints.Ok(entry.remapToNewAst(unwrappedAsts.value))
    }

    override val outputDescriptor = macroExpandedDependenciesDescriptor
    override val inputDescriptor =
            MultiValueDescriptor(listOf(configKey, macroExpandedAstKey, initialDependenciesKey)) {
                DependencyInput(
                        it[0] as CompilerConfig,
                        it[1] as ResultWithLints<List<Ast>>,
                        it[2] as ResultWithLints<List<DependencyEntry>>
                )
            }

}

class HirInput(val config: CompilerConfig, val finalGraph: ResultWithLints<List<DependencyEntry>>)

class HirLoweringQuery(val hirLowering: HirLowering) : Query<HirInput, ResultWithLints<List<HirFile>>> {
    override fun doQuery(input: HirInput): ResultWithLints<List<HirFile>> {
        val finalGraph = input.finalGraph
        if (finalGraph is ResultWithLints.Error) return ResultWithLints.Error(finalGraph.lints)
        finalGraph as ResultWithLints.Ok
        val config = input.config
        val entry = finalGraph.value[config.targetSourceIndex]
        return hirLowering.lower(entry as RealDependencyEntry)
    }

    override val outputDescriptor = hirDescriptor

    override val inputDescriptor =
            MultiValueDescriptor(listOf(configKey, macroExpandedDependenciesKey)) {
                HirInput(
                        it[0] as CompilerConfig,
                        it[1] as ResultWithLints<List<DependencyEntry>>
                )
            }
}



class MirLoweringQuery(val mirLowering: MirLowering) : Query<ResultWithLints<List<HirFile>>, ResultWithLints<List<MirFile>>> {
    override fun doQuery(input: ResultWithLints<List<HirFile>>): ResultWithLints<List<MirFile>> {
        if (input is ResultWithLints.Error) return ResultWithLints.Error(input.lints)
        input as ResultWithLints.Ok
        val hirFiles = input.value
        return ResultWithLints.Ok(mirLowering.lower(hirFiles))
    }

    override val outputDescriptor = mirDescriptor
    override val inputDescriptor = hirDescriptor

}


class QueryDrivenLispFrontendImpl(
        val lexer: Lexer,
        val parser: Parser,
        val tokenValidator: TokenValidator,
        val hirLowering: HirLowering,
        val lirLowering: LispLirLowering,
        val dependencyValidator: DependencyValidator,
        val macroExpander: MacroExpander,
        val mirLowering: MirLowering
) {

    fun compile(
            sources: List<Source>,
            stdlib: List<Source>,
            config: CompilerConfig
    ) {
        val database: Database = DatabaseImpl(listOf(
                SimpleValue(inputSourceDescriptor, sources),
                SimpleValue(stdlibSourceDescriptor, stdlib),
                SimpleValue(configDescriptor, config)
        ))
        database.registerQuery(MergedQuery())
        database.registerQuery(TokensQuery(lexer, tokenValidator))
        database.registerQuery(AstBuildingQuery(parser))
        database.registerQuery(InitialDependenciesQuery(dependencyValidator))
        database.registerQuery(MacroExpansionQuery(macroExpander))
        database.registerQuery(DependenciesRemappingQuery())
        database.registerQuery(HirLoweringQuery(hirLowering))
        database.registerQuery(MirLoweringQuery(mirLowering))

        val macroses = database.queryFor(mirDescriptor)
        val file = (macroses as ResultWithLints.Ok).value.first()
        val bbGraph = getBBGraph(file.functions[1])
//        File("graph.dot").writeText(bbGraph)
        println(bbGraph)
//        println(file)

    }
}

fun main(args: Array<String>) {
    val frontend = QueryDrivenLispFrontendImpl(
            LexerImpl(),
            Parser(),
            TokenValidator(),
            HirLowering(listOf()),
            LispLirLowering(),
            DependencyValidator(),
            MacroExpander(),
            MirLowering()
    )

    frontend.compile(listOf(InMemorySource("(defn + (x y) ())(defn foo (x) (if x (while #t ())  (if #t 2 3)))", "main")), listOf(), CompilerConfig(0))
}