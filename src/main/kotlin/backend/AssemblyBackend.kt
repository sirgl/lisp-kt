package backend

import lir.*

class AssemblyBackend : Backend {
    override fun runBackend(
        config: BackendConfiguration,
        file: LirFile,
        artifactBuilder: ArtifactBuilder
    ) : List<Artifact> {


        for (function in file.functions) {

        }
        return emptyList()
    }

    fun writeFunction(function: LirFunction) {
        for (instruction in function.instructions) {
            when (instruction) {
                is LirMovInstr -> TODO()
                is LirBinInstr -> TODO()
                is LirGetFunctionPtrInstr -> TODO()
                is LirGetStrPtrInstr -> TODO()
                is LirCallInstr -> TODO()
                is LirCondJumpInstr -> TODO()
                is LirReturnInstr -> TODO()
                is LirGotoInstr -> TODO()
                is LirInplaceI64 -> TODO()
                is LirLoadGlobalVar -> TODO()
            }
        }
    }

}