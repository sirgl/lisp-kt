package backend

import lir.World
import util.io.writeAll

class LlirPrintingBackend : Backend {
    override fun runBackend(config: BackendConfiguration, world: World, artifactBuilder: ArtifactBuilder): List<Artifact> {
        val artifacts = mutableListOf<Artifact>()
        for (unit in world.compilationUnits) {
            val artifact = artifactBuilder.createFileArtifact(unit.sourceFile) { outputStream ->
                // TODO toByteArray is not available in kotlin Common
                val bytes = unit.toString().toByteArray()
                outputStream.writeAll(bytes)
            }
            artifacts.add(artifact)
        }
        return artifacts
    }
}