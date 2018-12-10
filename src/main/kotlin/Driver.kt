import backend.*
import backend.codegen.TextAssembler
import deps.DependencyValidator
import frontend.CompilerConfig
import frontend.QueryDrivenLispFrontend
import hir.HirImport
import hir.HirLowering
import hir.HirValidator
import lexer.LexerImpl
import lexer.TokenValidator
import lir.LirLowering
import macro.MacroExpander
import mir.MirLowering
import parser.Parser
import shell.ShellCommandExecutor
import util.FileSource
import util.ResultWithLints
import util.Source
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList


class Driver {
    fun run(parsedArgs: Args) {
        val librarySources = findSources(parsedArgs.libraryPath)
        val programSources = findSources(parsedArgs.compilationPath)
        val mainFile = Paths.get(parsedArgs.mainFilePath)
        val indexOfTarget = programSources.indexOfFirst { it.path == mainFile.toString() }

        val frontend = QueryDrivenLispFrontend(
                LexerImpl(),
                Parser(),
                TokenValidator(),
                HirLowering(listOf(HirImport("stdlib", false))),
                LirLowering(),
                DependencyValidator(),
                HirValidator(),
                MacroExpander(),
                MirLowering()
        )
        val config = BackendConfiguration()
        val session = frontend.compilationSession(programSources, librarySources, CompilerConfig(indexOfTarget))
        val lirResult = session.getLir()
        for (lint in lirResult.lints) {
            System.err.println(lint)
        }
        if (lirResult is ResultWithLints.Error) {
            return
        }
        val lir = lirResult.unwrap()

        val backend = AssemblyBackend(TextAssembler(), NaiveRegisterAllocator())

        val artifactBuilder = PathArtifactBuilder(Paths.get(parsedArgs.outputPath))
        val artifacts = lir.flatMap { backend.runBackend(config, it, artifactBuilder) }
        val assemblyFilesToCompile = artifacts.filter { it.artifactType == ArtifactType.Assembly }

        val shellCommandExecutor = ShellCommandExecutor()
        shellCommandExecutor.runGcc(parsedArgs.compilationPath, assemblyFilesToCompile.map { it.path },
                parsedArgs.runtimePath)
    }
}


private fun findSources(path: String): List<Source> {
    return findSources(Paths.get(path))
}

private fun findSources(path: Path): List<Source> = Files.walk(path)
    .map { it.toString() }
    .filter { it.endsWith(".lisp") }
    .map { FileSource(it) }
    .toList()