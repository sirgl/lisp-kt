package shell

import java.io.File
import java.util.concurrent.TimeUnit

class ShellCommandExecutor {

    fun runGcc(compilationPath: String, assemblyFilesToCompile: List<String>, runtimePath: String): String {
        val commandParts = mutableListOf<String>()
        commandParts.add("gcc")
        for (asmFilePath in assemblyFilesToCompile) {
            commandParts.add(asmFilePath)
        }

        commandParts.add(runtimePath)
        return runCommand(commandParts, File(compilationPath))
    }

    fun runCommand(workingDir: File, command: String): String {
        val parts = splitConsideringQuotes(command)

        return runCommand(parts, workingDir)
    }

    private fun runCommand(parts: List<String>, workingDir: File): String {
        val proc = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectErrorStream(true)
                .start()

        proc.waitFor(1, TimeUnit.MINUTES)


        return proc.inputStream.bufferedReader().readText()
    }


    private fun splitConsideringQuotes(s: String): List<String> {
        val parts = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (i in 0 until s.length) {
            val ch = s[i]
            if (ch == ' ' && !inQuotes) {
                if (sb.isNotEmpty()) {
                    parts.add(sb.toString())
                    sb.clear()
                }
            } else {
                if (ch == '"' || ch == '\'') {
                    inQuotes = !inQuotes
                }
                sb.append(ch)
            }
        }
        if (sb.isNotEmpty()) {
            parts.add(sb.toString())
        }
        return parts
    }
}