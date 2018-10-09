package backend

import lir.World
import util.io.InputStream
import util.io.Path


interface Backend {
//    TODO need a way to manipulate output
    fun runBackend(config: BackendConfiguration, world: World, artifactBuilder: ArtifactBuilder)
}

class BackendConfiguration(val artifactDirectoryPath: String)

/**
 * Required to write output to array in tests
 */
interface ArtifactBuilder {
    /**
     * @param relativePath path relative to output directory
     */
    fun createFileArtifact(relativePath: Path): InputStream
}