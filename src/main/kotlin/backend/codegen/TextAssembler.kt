package backend.codegen

import backend.MemoryLocation
import util.collection.BiMap
import util.collection.HashBiMap
import util.io.OutputStream

class TextAssembler : AssemblerX64 {
    private val labels: BiMap<String, Label> = HashBiMap()

    private val sb = StringBuilder()
    override fun writeFunction(name: String, writer: (FunctionX64Assembler) -> Unit) {
        val functionAssembler = FunctionX64TextAssembler(labels, sb)
        functionAssembler.emitLabel(name)
        writer(functionAssembler)
        sb.append("\n")
    }

    override fun save(outputStream: OutputStream) {
        // TODO String is not convertible to byte array in common code
        outputStream.write(sb.toString().toByteArray())
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
        addLineShifted("mov %${from.presentableText}, %${to.presentableText}")
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
        // TODO add label
//        labels.put(name, Label())
//        addLine("$name:")
        TODO()
    }

    override fun emitCall(name: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun emitJmp(label: Label) {
        val name = labels.valueToKeyView[label] ?: throw AssemblerException("label $label not found")
        addLine("jmp $name")
    }

    override fun emitJmp(labelName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class LabelStorage() {
    fun nextLabel() : Label {
        TODO()
    }
}