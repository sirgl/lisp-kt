package backend.codegen

import backend.MemoryLocation
import java.io.OutputStream

class TextAssembler : Assembler {
    override fun writeStringTable(stringTable: Array<String>) {
        for (str in stringTable) {
            sb.append(".asciz \"$str\"\n")
        }
    }

    private val sb = StringBuilder()
    override fun writeFunction(name: String, writer: (FunctionAssembler) -> Unit) {
        val functionAssembler = FunctionTextAssembler(sb)
        functionAssembler.emitLabel(name)
        writer(functionAssembler)
        sb.append("\n")
    }

    override fun save(outputStream: OutputStream) {
        // TODO String is not convertible to byte array in common code
        outputStream.write(sb.toString().toByteArray())
        sb.clear()
    }
}

class FunctionTextAssembler(
        private val sb: StringBuilder
) : FunctionAssembler {
    override fun emitCmpWithZero(memoryLocation: MemoryLocation) {
        addLineShifted("cmpq $0, ${memoryLocation.assemblyText}")
    }

    override fun emitMov(from: MemoryLocation, to: MemoryLocation) {
        addLineShifted("movq ${from.assemblyText}, ${to.assemblyText}")
    }

    override fun emitComment(text: String) {
        addLine(""";$text""")
    }

    override fun emitRet() {
        addLineShifted("retq")
    }

    override fun emitMov(immediate: Long, to: MemoryLocation) {
        addLineShifted("movq \$$immediate, ${to.assemblyText}")
    }

    override fun emitMov(functionName: String, to: MemoryLocation) {
        addLineShifted("movq $functionName, ${to.assemblyText}")
    }

    override fun emitMovabs(stringLabel: String, to: MemoryLocation) {
        addLineShifted("movabsq $stringLabel, ${to.assemblyText}")
    }

    override fun emitPush(memoryLocation: MemoryLocation) {
        addLineShifted("pushq ${memoryLocation.assemblyText}")
    }

    override fun emitPop(memoryLocation: MemoryLocation) {
        addLineShifted("popq ${memoryLocation.assemblyText}")
    }

    override fun emitLabel(name: String) {
        addLine("$name:")
    }

    override fun emitCall(name: String) {
        addLineShifted("callq $name")
    }

    override fun emitJmp(label: String) {
        addLineShifted("jmp $label")
    }

    override fun emitJne(label: String) {
        addLineShifted("jne $label")
    }

    override fun emitAdd(value: Int, destination: MemoryLocation) {
        addLineShifted("addq \$$value, ${destination.assemblyText}")
    }

    override fun emitSub(value: Int, destination: MemoryLocation) {
        addLineShifted("subq \$$value, ${destination.assemblyText}")
    }

    private fun addLine(text: String) {
        sb.append(text).append("\n")
    }

    private fun addLineShifted(text: String) {
        sb.append("\t").append(text).append("\n")
    }
}