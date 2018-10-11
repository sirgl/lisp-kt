package backend

import backend.codegen.TextAssembler
import backend.x64.X64Registers
import lir.World

class AssemblyBackend : Backend {
    override fun runBackend(
            config: BackendConfiguration,
            world: World,
            artifactBuilder: ArtifactBuilder
    ) : List<Artifact> {
        for (unit in world.compilationUnits) {
            val unitAssembler = TextAssembler()
            for (function in unit.functions) {
                unitAssembler.writeFunction(function.name) {
                    // TODO
                    it.emitMov(X64Registers.rdi, X64Registers.rax)
                    it.emitRet()
                }
            }
            artifactBuilder.createFileArtifact(unit.sourceFile) { os -> unitAssembler.save(os) }
        }
        return emptyList()
    }

    fun writeFunction() {

    }

}