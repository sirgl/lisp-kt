package mir

// Usually we don't always have full information about something.
// To make render of instruction more flexible we have separate renderer for each component.
class PrettyPrintStrategy(
        val instrTypeRenderer: MirInstrTypeRenderer,
        val instrIdRenderer: MirInstrIdRenderer,
        val functionIdRenderer: MirFunctionIdRenderer,
        val varIdRenderer: MirVarIdRenderer,
        val additionalInfoRenderer: MirAdditionalInfoRenderer,
        val blockIndexRenderer: MirBlockIndexRenderer
)

interface MirInstrTypeRenderer {
    fun render(instr: MirInstr) : String
}

interface MirInstrIdRenderer {
    fun render(id: MirInstrId) : String
}

interface MirFunctionIdRenderer {
    fun render(functionId: Int) : String
}

interface MirVarIdRenderer {
    fun render(varId: Short) : String
}

interface MirAdditionalInfoRenderer {
    fun render(instr: MirInstr) : String
}

interface MirBlockIndexRenderer {
    fun render(index: Short) : String
}

private object DefaultInstrTypeRenderer : MirInstrTypeRenderer {
    override fun render(instr: MirInstr): String = ""
}

private object DefaultInstrIdRenderer : MirInstrIdRenderer {
    override fun render(id: MirInstrId): String = id.toString()
}

private object DefaultFunctionIdRenderer : MirFunctionIdRenderer {
    override fun render(functionId: Int): String = "b" + functionId.toString()
}

private object DefaultVarIdRenderer : MirVarIdRenderer {
    override fun render(varId: Short): String = "v" + varId.toString()
}

private object DefaultAdditionalInfoRenderer : MirAdditionalInfoRenderer {
    override fun render(instr: MirInstr): String = ""
}

private object DefaultBlockIndexRenderer : MirBlockIndexRenderer {
    override fun render(index: Short): String = index.toString()
}

val defaultPrintStrategy = PrettyPrintStrategy(
        DefaultInstrTypeRenderer,
        DefaultInstrIdRenderer,
        DefaultFunctionIdRenderer,
        DefaultVarIdRenderer,
        DefaultAdditionalInfoRenderer,
        DefaultBlockIndexRenderer
)