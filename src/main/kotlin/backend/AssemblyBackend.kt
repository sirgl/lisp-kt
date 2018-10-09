package backend

import lir.World

class AssemblyBackend : Backend {
    override fun runBackend(config: BackendConfiguration, world: World, artifactBuilder: ArtifactBuilder) : List<Artifact> {
        for (unit in world.compilationUnits) {
            for (function in unit.functions) {

            }
        }
        return emptyList()
    }

}