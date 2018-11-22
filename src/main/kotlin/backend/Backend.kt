package backend

import lir.LirFile
import java.io.OutputStream
import java.nio.file.Path


interface Backend {
//    TODO need a way to manipulate output
    fun runBackend(config: BackendConfiguration, file: LirFile, artifactBuilder: ArtifactBuilder) : List<Artifact>
}

class BackendConfiguration(val artifactDirectoryPath: String)

