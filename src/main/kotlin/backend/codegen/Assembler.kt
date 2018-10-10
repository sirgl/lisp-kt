package backend.codegen

import backend.MemoryLocation
import util.LongStorage

// TODO think how to extract result from
interface AssemblerX64 {

    fun writeFunction(writer: (FunctionX64Assembler) -> Unit)
}

interface FunctionX64Assembler {
    // TODO forbid from mem to mem
    fun emitMov(from: MemoryLocation, to: MemoryLocation)

    fun emitRet()

    // TODO not register?
    fun emitPush(memoryLocation: MemoryLocation)

    fun emitPop(memoryLocation: MemoryLocation)

    fun emitLabel(name: String): Label
}

inline class Label(private val storage: LongStorage) {
    constructor(offset: Int, id: Int) : this(LongStorage(offset, id))

    val offset: Int
        get() = storage.first

    val id: Int
        get() = storage.second
}