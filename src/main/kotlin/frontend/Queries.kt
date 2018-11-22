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
import lir.LirFile
import lir.LirLowering
import macro.MacroExpander
import mir.MirFile
import mir.MirLowering
import parser.Ast
import parser.ParseResult
import parser.Parser
import query.*
import util.InMemorySource
import util.ResultWithLints
import util.Source

val inputSourceDescriptor = TypedKey<List<Source>>("sources")
val stdlibSourceDescriptor = TypedKey<List<Source>>("stdlib")
val mergedSourceDescriptor = TypedKey<List<Source>>("merged")
val configDescriptor = TypedKey<CompilerConfig>("config")
val tokensDescriptor = TypedKey<ResultWithLints<TokenizedSources>>("tokens")
val astsDescriptor = TypedKey<ResultWithLints<List<Ast>>>("asts")
val initialDependenciesDescriptor = TypedKey<ResultWithLints<List<DependencyEntry>>>("initial deps")
val macroExpandedAstDescriptor = TypedKey<ResultWithLints<List<Ast>>>("macro expanded ast")
val macroExpandedDependenciesDescriptor = TypedKey<ResultWithLints<List<DependencyEntry>>>("macro expanded dependencies")
val hirDescriptor = TypedKey<ResultWithLints<List<HirFile>>>("hir")
val mirDescriptor = TypedKey<ResultWithLints<List<MirFile>>>("mir")
val lirDescriptor = TypedKey<ResultWithLints<List<LirFile>>>("lir")


class MergedQuery : Query<List<Source>> {
    override fun doQuery(input: TypedStorage): List<Source> {
        return input[stdlibSourceDescriptor] + input[inputSourceDescriptor]
    }

    override val outputDescriptor = mergedSourceDescriptor
    override val inputKey = MultiKey("sources", listOf(stdlibSourceDescriptor, inputSourceDescriptor))
    override val name: String?
        get() = "Merge sources from stdlib and user ones"
}

class TokenizedSources(val sourcesWithTokens: List<Pair<Source, List<Token>>>)

class TokenizationQuery(
    val lexer: Lexer,
    val tokenValidator: TokenValidator
) : SimpleQuery<List<Source>, ResultWithLints<TokenizedSources>>("Tokenization") {
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

    override val outputDescriptor = tokensDescriptor

    override val inputKey = mergedSourceDescriptor

}

class AstBuildingQuery(
    val parser: Parser
) : SimpleQuery<ResultWithLints<TokenizedSources>, ResultWithLints<List<Ast>>>("Ast building") {
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
                    val parseLint =
                        Lint(parseResult.text, parseResult.textRange, Severity.Error, Subsystem.Parser, source)
                    return ResultWithLints.Error(lints + parseLint)
                }
            }
            Ast(fileNode, source)
        })
    }

    override val outputDescriptor = astsDescriptor
    override val inputKey = tokensDescriptor
}

class InitialDependenciesQuery(
    val dependencyValidator: DependencyValidator
) : SimpleQuery<ResultWithLints<List<Ast>>, ResultWithLints<List<DependencyEntry>>>("Build dependencies for inintial AST") {
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
    override val inputKey = astsDescriptor
}

class MacroExpansionQuery(
    val macroExpander: MacroExpander
) : Query<ResultWithLints<List<Ast>>> {
    override fun doQuery(input: TypedStorage): ResultWithLints<List<Ast>> {
        val initialAsts = input[astsDescriptor]
        if (initialAsts is ResultWithLints.Error) return ResultWithLints.Error(initialAsts.lints)
        val dependecies = input[initialDependenciesDescriptor]
        if (dependecies is ResultWithLints.Error) return ResultWithLints.Error(dependecies.lints)
        initialAsts as ResultWithLints.Ok
        dependecies as ResultWithLints.Ok
        val asts = initialAsts.value
        val targetSourceIndex = input[configDescriptor].targetSourceIndex
        val deps = dependecies.value
        val targetSourceEntry = deps[targetSourceIndex]
        return macroExpander.expand(asts, targetSourceEntry)
    }

    override val outputDescriptor = macroExpandedAstDescriptor
    override val inputKey = MultiKey("macro input",
        listOf(configDescriptor, astsDescriptor, initialDependenciesDescriptor))

    override val name: String?
        get() = "Macro expansion"
}

class DependenciesRemappingQuery : Query<ResultWithLints<List<DependencyEntry>>> {
    override fun doQuery(input: TypedStorage): ResultWithLints<List<DependencyEntry>> {
        val unwrappedAsts = input[macroExpandedAstDescriptor]
        if (unwrappedAsts is ResultWithLints.Error) return ResultWithLints.Error(unwrappedAsts.lints)
        val initialDependencies = input[initialDependenciesDescriptor]
        if (initialDependencies is ResultWithLints.Error) return ResultWithLints.Error(initialDependencies.lints)
        unwrappedAsts as ResultWithLints.Ok
        initialDependencies as ResultWithLints.Ok
        val config = input[configDescriptor]
        val initialDeps = initialDependencies.value
        val entry = initialDeps[config.targetSourceIndex]
        return ResultWithLints.Ok(entry.remapToNewAst(unwrappedAsts.value))
    }

    override val outputDescriptor = macroExpandedDependenciesDescriptor
    override val inputKey = MultiKey("Dependencies remap input",
        listOf(configDescriptor, macroExpandedAstDescriptor, initialDependenciesDescriptor))

    override val name: String?
        get() = "Dependencies remapping"
}

class HirLoweringQuery(val hirLowering: HirLowering) : Query<ResultWithLints<List<HirFile>>> {
    override fun doQuery(input: TypedStorage): ResultWithLints<List<HirFile>> {
        val finalGraph = input[macroExpandedDependenciesDescriptor]
        if (finalGraph is ResultWithLints.Error) return ResultWithLints.Error(finalGraph.lints)
        finalGraph as ResultWithLints.Ok
        val config = input[configDescriptor]
        val entry = finalGraph.value[config.targetSourceIndex]
        return hirLowering.lower(entry as RealDependencyEntry)
    }

    override val outputDescriptor = hirDescriptor

    override val inputKey =
        MultiKey("Hir lowering descriptor", listOf(configDescriptor, macroExpandedDependenciesDescriptor))

    override val name: String?
        get() = "Hir lowering"
}


class MirLoweringQuery(val mirLowering: MirLowering) :
    SimpleQuery<ResultWithLints<List<HirFile>>, ResultWithLints<List<MirFile>>>("Mir lowering") {
    override fun doQuery(input: ResultWithLints<List<HirFile>>): ResultWithLints<List<MirFile>> {
        if (input is ResultWithLints.Error) return ResultWithLints.Error(input.lints)
        input as ResultWithLints.Ok
        val hirFiles = input.value
        return ResultWithLints.Ok(mirLowering.lower(hirFiles))
    }

    override val outputDescriptor = mirDescriptor
    override val inputKey = hirDescriptor

}


class LirLoweringQuery(val lirLowering: LirLowering) :
    SimpleQuery<ResultWithLints<List<MirFile>>, ResultWithLints<List<LirFile>>>("Lir lowering") {
    override fun doQuery(input: ResultWithLints<List<MirFile>>): ResultWithLints<List<LirFile>> {
        if (input is ResultWithLints.Error) return ResultWithLints.Error(input.lints)
        input as ResultWithLints.Ok
        val mirFiles = input.value
        return ResultWithLints.Ok(lirLowering.lower(mirFiles))
    }

    override val outputDescriptor = lirDescriptor
    override val inputKey = mirDescriptor

}


class QueryDrivenLispFrontend(
    val lexer: Lexer,
    val parser: Parser,
    val tokenValidator: TokenValidator,
    val hirLowering: HirLowering,
    val lirLowering: LirLowering,
    val dependencyValidator: DependencyValidator,
    val macroExpander: MacroExpander,
    val mirLowering: MirLowering
) {

    fun compilationSession(
        sources: List<Source>,
        stdlib: List<Source>,
        config: CompilerConfig
    ): CompilationSession {
        return CompilationSession(this, sources, stdlib, config)
    }

    fun compile(
        sources: List<Source>,
        stdlib: List<Source>,
        config: CompilerConfig
    ) {
//        val macroses = compilationSession().getMir()
//        val file = (macroses as ResultWithLints.Ok).value.first()
//        val bbGraph = getBBGraph(file.functions[1])
//        File("graph.dot").writeText(bbGraph)
//        println(bbGraph)
//        println(file)

    }
}

class CompilationSession(
    val frontend: QueryDrivenLispFrontend,
    val sources: List<Source>,
    val stdlib: List<Source>,
    val config: CompilerConfig
) {
    fun getDb(): Database {
        val database: Database = DatabaseImpl(
            listOf(
                DatabaseValue(inputSourceDescriptor, sources),
                DatabaseValue(stdlibSourceDescriptor, stdlib),
                DatabaseValue(configDescriptor, config)
            )
        )
        with(frontend) {
            database.registerQuery(MergedQuery())
            database.registerQuery(TokenizationQuery(lexer, tokenValidator))
            database.registerQuery(AstBuildingQuery(parser))
            database.registerQuery(InitialDependenciesQuery(dependencyValidator))
            database.registerQuery(MacroExpansionQuery(macroExpander))
            database.registerQuery(DependenciesRemappingQuery())
            database.registerQuery(HirLoweringQuery(hirLowering))
            database.registerQuery(MirLoweringQuery(mirLowering))
            database.registerQuery(LirLoweringQuery(lirLowering))
            return database
        }
    }

    fun getMir(): ResultWithLints<List<MirFile>> {
        return getDb().queryFor(mirDescriptor)
    }

    fun getLir(): ResultWithLints<List<LirFile>> {
        return getDb().queryFor(lirDescriptor)
    }
}

fun main(args: Array<String>) {
    val frontend = QueryDrivenLispFrontend(
        LexerImpl(),
        Parser(),
        TokenValidator(),
        HirLowering(listOf()),
        LirLowering(),
        DependencyValidator(),
        MacroExpander(),
        MirLowering()
    )

//    frontend.compile(listOf(InMemorySource("(defn + (x y) ())(defn foo (x) (if x (while #t ())  (if #t 2 3)))", "main")), listOf(), CompilerConfig(0))
    val session = frontend.compilationSession(
        listOf(InMemorySource("(defn + (x y) ())(defn foo (x) (if x (while #t ())  (if #t 2 3)))", "main")),
        listOf(),
        CompilerConfig(0)
    )
    println((session.getDb() as DatabaseImpl).getGraphvizOfAllQueries())
}