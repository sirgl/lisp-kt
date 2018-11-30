import backend.*
import backend.codegen.TextAssembler
import deps.DependencyValidator
import frontend.CompilerConfig
import frontend.QueryDrivenLispFrontend
import hir.HirImport
import hir.HirLowering
import hir.HirValidator
import kotlinx.cli.CommandLineInterface
import kotlinx.cli.flagValueArgument
import kotlinx.cli.parse
import lexer.LexerImpl
import lexer.TokenValidator
import lir.LirLowering
import macro.MacroExpander
import mir.MirLowering
import parser.Parser
import util.FileSource
import util.ResultWithLints
import util.Source
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList
import kotlin.system.exitProcess


private class Args(
    val mainFilePath: String,
    val compilationPath: String,
    val libraryPath: String,
    // TODO Should it be object file or not?
    val runtimePath: String,
    val outputPath: String
)

private fun parseArgs(args: Array<String>): Args {
    val cli = CommandLineInterface("Lisp-kt")
    val mainFilePath by cli.flagValueArgument("-m", "string", "Path to main file")
    val compilationPath by cli.flagValueArgument("-p", "string", "Path to the files available for compilation", "")
    val libraryPath by cli.flagValueArgument("-l", "string", "Path to the standard library sources")
    val outputPath by cli.flagValueArgument("-o", "string", "Path to the output directory", ".")
    val runtimePath by cli.flagValueArgument("-r", "string", "Path to the runtime object file")

    try {
        cli.parse(args)
    } catch (e: Exception) {
        exitProcess(1)
    }

    if (libraryPath == null) {
        System.err.println("Path to stdlib must be supplied (flag: -l)")
        exitProcess(1)
    }
    if (mainFilePath == null) {
        System.err.println("Path to main file must be supplied (flag: -m)")
        exitProcess(1)
    }
    if (runtimePath == null) {
        System.err.println("Path to runtime object file must be supplied (flag: -r)")
        exitProcess(1)
    }
    return Args(mainFilePath!!, compilationPath, libraryPath!!, outputPath, runtimePath!!)
}

fun main(args: Array<String>) {
    val parsedArgs = parseArgs(args)

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
    // TODO run gcc to assemble final version

}

private fun findSources(path: String): List<Source> {
    return findSources(Paths.get(path))
}

private fun findSources(path: Path): List<Source> = Files.walk(path)
    .map { it.toString() }
    .filter { it.endsWith(".lisp") }
    .map { FileSource(it) }
    .toList()