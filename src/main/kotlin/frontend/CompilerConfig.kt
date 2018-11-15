package frontend

class CompilerConfig(
        val targetSourceIndex: Int,
        val dumpTokens: Boolean = false,
        val dumpAstAfterParse: Boolean = false,
        val dumpAstAfterExpansion: Boolean = false,
        val dumpHir: Boolean = false,
        val dumpMir: Boolean = false,
        val dumpMirInEachPhase: Boolean = false,
        val dumpLir: Boolean = false
)