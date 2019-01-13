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
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList


class Driver {
    fun run(parsedArgs: Args) : Boolean {
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
            return false
        }
        val lir = lirResult.unwrap()

        val backend = AssemblyBackend(TextAssembler(), NaiveRegisterAllocator())

        val artifactBuilder = PathArtifactBuilder(Paths.get(parsedArgs.outputPath))
        val artifacts = lir.flatMap { backend.runBackend(config, it, artifactBuilder) }
        val assemblyFilesToCompile = artifacts.filter { it.artifactType == ArtifactType.Assembly }

        val shellCommandExecutor = ShellCommandExecutor()
        val cmakePath = "${parsedArgs.outputPath}/CMakeLists.txt"
        // TODO abstract it
        File(cmakePath).writeText("""
cmake_minimum_required(VERSION 3.10)
project(runtime)
enable_language(C CXX ASM)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY /home/roman/IdeaProjects/lisp-kt/src/test/resources/integration/base)

set(library_src
    "/home/roman/IdeaProjects/lisp-kt/runtime/Error.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/Error.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/Gc.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/Gc.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/main.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/StdLib.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/StdLib.h"

    "/home/roman/IdeaProjects/lisp-kt/runtime/gc/RootContributor.h"

    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Allocation.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Allocation.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/List.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/List.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Memory.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Memory.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/String.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/String.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Symbol.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Symbol.h"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Types.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/memory/Types.h"

    "/home/roman/IdeaProjects/lisp-kt/runtime/utils/Utils.cpp"
    "/home/roman/IdeaProjects/lisp-kt/runtime/utils/Utils.h"
)

set(SRC_FILES
    /home/roman/IdeaProjects/lisp-kt/src/test/resources/integration/base/src/test/resources/integration/base/Main.lisp.S
    /home/roman/IdeaProjects/lisp-kt/src/test/resources/integration/base/src/main/resources/stdlib.lisp.S
)

add_executable(runtime ${'$'}{SRC_FILES} ${'$'}{library_src})
        """.trimIndent())
        shellCommandExecutor.runCommand(File("."), "cmake --build /home/roman/IdeaProjects/lisp-kt/src/test/resources/integration/base --target runtime -- -j 2")
//        val compilationLogs = shellCommandExecutor.runGcc(parsedArgs.compilationPath, assemblyFilesToCompile.map { it.path },
//                parsedArgs.runtimePath)
//        println(compilationLogs)
        return true
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