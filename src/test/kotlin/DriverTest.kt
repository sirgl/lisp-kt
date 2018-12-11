import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import shell.ShellCommandExecutor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.test.assertEquals


class DriverTest {
    val uut: Driver = Driver()
    private val shellCommandExecutor: ShellCommandExecutor = ShellCommandExecutor()
    private val libraryPath = "src/main/resources/stdlib.lisp"
    private val runtimePath = "src/test/resources/runtime.o"

    @ParameterizedTest
    @MethodSource("testPathProvider")
    fun run(path: Path) {
        val mainFilePath = path.resolve("Main.lisp").toString()
        val outputFileName = path.last().toString()
        val workingDirectory = path.toString()
        val success = uut.run(
            Args(
                mainFilePath, workingDirectory, libraryPath, runtimePath, workingDirectory, outputFileName
            )
        )
        if (!success) throw IllegalStateException("Failed to compile")
        val actual = shellCommandExecutor.runCommand(path.toFile(), "./$outputFileName.o")
        val expected = Files.readAllLines(path.resolve("expected.txt")).joinToString("\n")
        assertEquals(expected, actual)
    }

    companion object {
        @JvmStatic
        fun testPathProvider(): Stream<Arguments> {
            val testCasesDir = Paths.get("src/test/resources/integration")
            return Files.list(testCasesDir)
                    .filter { Files.isDirectory(it) }
                    .map { Arguments.of(it) }
        }
    }
}