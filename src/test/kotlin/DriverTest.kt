import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream


class DriverTest {

    @ParameterizedTest
    fun run() {

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