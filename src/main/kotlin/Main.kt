import kotlinx.cli.CommandLineInterface
import kotlinx.cli.flagValueArgument
import kotlinx.cli.parse
import kotlin.system.exitProcess

class Args(
        val mainFilePath: String,
        val compilationPath: String,
        val libraryPath: String,
        // TODO Should it be object file or not?
        val runtimePath: String,
        val outputPath: String,
        val outputFileName: String
)

private fun parseArgs(args: Array<String>): Args {
    val cli = CommandLineInterface("Lisp-kt")
    val mainFilePath by cli.flagValueArgument("-m", "string", "Path to main file")
    val compilationPath by cli.flagValueArgument("-p", "string", "Path to the files available for compilation", "")
    val libraryPath by cli.flagValueArgument("-l", "string", "Path to the standard library sources")
    val outputPath by cli.flagValueArgument("-o", "string", "Path to the output directory", ".")
    val runtimePath by cli.flagValueArgument("-r", "string", "Path to the runtime object file")
    val outputFileName by cli.flagValueArgument("-of", "string", "Path to the runtime object file", "lisp-kt.o")

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
    return Args(mainFilePath!!, compilationPath, libraryPath!!, outputPath, runtimePath!!, outputFileName)
}

fun main(args: Array<String>) {
    val parsedArgs = parseArgs(args)

    Driver().run(parsedArgs)
}