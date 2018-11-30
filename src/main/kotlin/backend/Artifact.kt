package backend

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.lang.StringBuilder
import java.nio.file.Path


enum class ArtifactType {
    MacroExpanded,
    HirDump,
    MirDump,
    LirDump,
    Assembly
}

// Not Java Path because of cross platform
/**
 * path relative from output directory
 */
class FileArtifact (
    val path: String,
    val artifactType: ArtifactType
)

/**
 * Required to write output to array in tests
 */
interface ArtifactBuilder {
    /**
     * @param relativePath path relative to output directory
     */
    fun createFileArtifact(relativePath: String, filler: (OutputStream) -> Unit, type: ArtifactType = ArtifactType.Assembly) : FileArtifact
}

class StringArtifactBuilder : ArtifactBuilder {
    val sb = StringBuilder()

    override fun createFileArtifact(relativePath: String, filler: (OutputStream) -> Unit, type: ArtifactType): FileArtifact {
        sb.append("$relativePath:\n")
        val os = ByteArrayOutputStream()
        filler(os)
        sb.append(os.toString())
        return FileArtifact(relativePath, type)
    }

}

class PathArtifactBuilder(private val directory: Path) : ArtifactBuilder {
    override fun createFileArtifact(relativePath: String, filler: (OutputStream) -> Unit, type: ArtifactType): FileArtifact {
        val path = directory.resolve(relativePath)
        val file = File(path.toString())
        filler(file.outputStream())
        return FileArtifact(path.toString(), type)
    }
}




//class AssemblyFileArtifact(path: Path, val assembler: Assembler) : FileArtifact(path)