package backend

import lir.LirFile


interface Backend {
    fun runBackend(config: BackendConfiguration, file: LirFile, artifactBuilder: ArtifactBuilder) : List<FileArtifact>
}

class BackendConfiguration

