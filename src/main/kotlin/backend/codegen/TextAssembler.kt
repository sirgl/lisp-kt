package backend.codegen

import backend.MemoryLocation
import util.collection.BiMap
import util.collection.HashBiMap

class TextAssembler : AssemblerX64 {
    private val labels: BiMap<String, Label> = HashBiMap()
    private val sb = StringBuilder()

    override fun writeFunction(writer: (FunctionX64Assembler) -> Unit) {
        val functionAssembler = FunctionX64TextAssembler(labels, sb)
        writer(functionAssembler)
        sb.append("\n")
    }

}

class FunctionX64TextAssembler(
        private val labels: BiMap<String, Label>,
        private val sb: StringBuilder
) : FunctionX64Assembler {
    private fun addLine(text: String) {
        sb.append(text).append("\n")
    }

    private fun addLineShifted(text: String) {
        sb.append("\t").append(text).append("\n")
    }

    override fun emitMov(from: MemoryLocation, to: MemoryLocation) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun emitRet() {
        addLineShifted("ret")
    }

    override fun emitPush(memoryLocation: MemoryLocation) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun emitPop(memoryLocation: MemoryLocation) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun emitLabel(name: String): Label {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun emitCall(name: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun emitJmp(label: Label) {
        val name = labels.valueToKeyView[label] ?: throw AssemblerException("label $label not found")
        addLine("$name:")
    }

    override fun emitJmp(labelName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}