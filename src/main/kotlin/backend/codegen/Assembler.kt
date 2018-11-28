package backend.codegen

import backend.MemoryLocation
import util.LongStorage
import java.io.OutputStream

interface Assembler {

    fun writeStringTable(stringTable: Array<String>)

    fun writeFunction(name: String, writer: (FunctionAssembler) -> Unit)

    fun save(outputStream: OutputStream)
}

interface FunctionAssembler {
    // TODO forbid from mem to mem
    fun emitMov(from: MemoryLocation, to: MemoryLocation)
    fun emitMov(immediate: Long, to: MemoryLocation)
    fun emitMovabs(stringLabel: String, to: MemoryLocation)

    fun emitMov(functionName: String, to: MemoryLocation)

    fun emitRet()

    fun emitPush(memoryLocation: MemoryLocation)

    fun emitPop(memoryLocation: MemoryLocation)

    fun emitLabel(name: String)

    fun emitCall(name: String)

    fun emitCmpWithZero(memoryLocation: MemoryLocation)

    fun emitJmp(label: String)
    fun emitJne(label: String)

    fun emitSub(value: Int, destination: MemoryLocation)
    fun emitAdd(value: Int, destination: MemoryLocation)
}