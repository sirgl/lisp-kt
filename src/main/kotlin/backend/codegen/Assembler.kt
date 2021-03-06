package backend.codegen

import backend.MemoryLocation
import hir.HirMacroasmDefinition
import util.LongStorage
import java.io.OutputStream

interface Assembler {

    fun writeStringTable(stringTable: Array<String>)

    fun writeFunction(name: String, writer: (FunctionAssembler) -> Unit)

    fun writeMacroasm(def: HirMacroasmDefinition)

    fun markAsText()

    fun save(outputStream: OutputStream)

    fun writeExportTable(functionNames: List<String>)
}

interface FunctionAssembler {
    // TODO forbid from mem to mem
    fun emitMov(from: MemoryLocation, to: MemoryLocation)
    fun emitComment(text: String)
    fun emitMov(immediate: Long, to: MemoryLocation)
    fun emitMovabs(stringLabel: String, to: MemoryLocation)
    fun emitMovabs(from: MemoryLocation, to: MemoryLocation)
    fun emitMovabs(immediate: Long, to: MemoryLocation)

    fun emitMov(text: String, to: MemoryLocation)

    fun emitRet()

    fun emitPush(memoryLocation: MemoryLocation)

    fun emitPop(memoryLocation: MemoryLocation)

    fun emitLabel(name: String)

    fun emitCall(name: String)
    fun emitCallByPtrRax()

    fun emitCmpWithZero(memoryLocation: MemoryLocation)

    fun emitJmp(label: String)
    fun emitJe(label: String)

    fun emitSub(value: Int, destination: MemoryLocation)
    fun emitAdd(value: Int, destination: MemoryLocation)
}