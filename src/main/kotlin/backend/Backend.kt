package backend

import lir.World


interface Backend {
    fun runBackend(config: BackendConfiguration, world: World) : List<Artifact>
}

class BackendConfiguration(val artifactDirectoryPath: String)