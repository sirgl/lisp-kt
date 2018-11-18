package backend

import lir.LirFile
import java.io.OutputStream
import java.nio.file.Path


interface Backend {
//    TODO need a way to manipulate output
    fun runBackend(config: BackendConfiguration, file: LirFile, artifactBuilder: ArtifactBuilder) : List<Artifact>
}

class BackendConfiguration(val artifactDirectoryPath: String)

/**
 * Required to write output to array in tests
 */
interface ArtifactBuilder {
    /**
     * @param relativePath path relative to output directory
     */
    fun createFileArtifact(relativePath: String, filler: (OutputStream) -> Unit) : FileArtifact
}