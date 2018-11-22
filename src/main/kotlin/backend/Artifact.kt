package backend

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.lang.StringBuilder

/**
 * It is files and other information that we can get from backend as a result of compilation
 */
interface Artifact

// Not Java Path because of cross platform
interface FileArtifact : Artifact {
    /**
     * path relative from output directory
     */
    val path: String
}

/**
 * Required to write output to array in tests
 */
interface ArtifactBuilder {
    /**
     * @param relativePath path relative to output directory
     */
    fun createFileArtifact(relativePath: String, filler: (OutputStream) -> Unit) : FileArtifact
}

class StringArtifactBuilder : ArtifactBuilder {
    val sb = StringBuilder()

    override fun createFileArtifact(relativePath: String, filler: (OutputStream) -> Unit): FileArtifact {
        sb.append("$relativePath:\n")
        val os = ByteArrayOutputStream()
        filler(os)
        sb.append(os.toString())
        return InMemoryFileArtifact(relativePath)
    }

}

class InMemoryFileArtifact(override val path: String) : FileArtifact

//class AssemblyFileArtifact(path: Path, val assembler: Assembler) : FileArtifact(path)