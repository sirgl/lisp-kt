package backend.codegen

import backend.MemoryLocation
import backend.mangle
import hir.HirMacroasmDefinition
import java.io.OutputStream

class TextAssembler : Assembler {
	override fun writeMacroasm(def: HirMacroasmDefinition) {
		sb.append("${def.name}:\n${def.asmText}\n")
	}

	override fun writeExportTable(functionNames: List<String>) {
        for (functionName in functionNames) {
            sb.append("\t.globl $functionName\n")
        }
    }

    override fun markAsText() {
        sb.append("\t.text\n")
    }

    override fun writeStringTable(stringTable: Array<String>) {
        for ((index, str) in stringTable.withIndex()) {
            sb.append("Lstr$index:\n")
            sb.append("\t.asciz \"$str\"\n")
        }
    }

    private val sb = StringBuilder()
    override fun writeFunction(name: String, writer: (FunctionAssembler) -> Unit) {
        val functionAssembler = FunctionTextAssembler(sb)
        functionAssembler.emitLabel(mangle(name))
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
        addLine("""//$text""")
    }

    override fun emitMovabs(from: MemoryLocation, to: MemoryLocation) {
        addLineShifted("movabsq ${from.assemblyText}, ${to.assemblyText}")
    }

    override fun emitMovabs(immediate: Long, to: MemoryLocation) {
        addLineShifted("movabsq \$$immediate, ${to.assemblyText}")
    }

    override fun emitRet() {
        addLineShifted("retq")
    }

    override fun emitMov(immediate: Long, to: MemoryLocation) {
        addLineShifted("movq \$$immediate, ${to.assemblyText}")
    }

    override fun emitMov(text: String, to: MemoryLocation) {
        addLineShifted("movq $text, ${to.assemblyText}")
    }

    override fun emitCallByPtrRax() {
        addLineShifted("call *%rax")
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

    override fun emitJe(label: String) {
        addLineShifted("je $label")
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