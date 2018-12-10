import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import shell.ShellCommandExecutor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream


class DriverTest {
    val uut: Driver = Driver()
    private val libraryPath = "src/main/resources/stdlib.lisp"
    private val shellCommandExecutor: ShellCommandExecutor = ShellCommandExecutor()

    @ParameterizedTest
    @MethodSource("testPathProvider")
    fun run(path: Path) {
        val mainFilePath = path.resolve("Main.lisp").toString()
        val outputFileName = path.last().toString()
        val workingDirectory = path.toString()
        uut.run(Args(mainFilePath, workingDirectory, libraryPath, "src/test/resources/runtime.o",
                workingDirectory, outputFileName))
        val result = shellCommandExecutor.runCommand(path.toFile(), "./$outputFileName")
//        Paths.get()
    }

    companion object {
        @JvmStatic
        fun testPathProvider(): Stream<Arguments>? {
            val testCasesDir = Paths.get("src/test/resources/integration")
            return Files.list(testCasesDir)
                    .filter { Files.isDirectory(it) }
                    .map { Arguments.of(it) }

        }
    }
}