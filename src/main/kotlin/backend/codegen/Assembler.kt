package backend.codegen

import backend.MemoryLocation
import util.LongStorage
import java.io.OutputStream

// TODO think how to extract result from
interface Assembler {

    fun writeFunction(name: String, writer: (FunctionAssembler) -> Unit)

    fun save(outputStream: OutputStream)
}

interface FunctionAssembler {
    // TODO forbid from mem to mem
    // TODO not only memory, it can contains offsets, derefs and so on
    fun emitMov(from: MemoryLocation, to: MemoryLocation)

    fun emitRet()

    // TODO not register?
    fun emitPush(memoryLocation: MemoryLocation)

    fun emitPop(memoryLocation: MemoryLocation)

    fun emitLabel(name: String): Label

    fun emitCall(name: String)

    fun emitJmp(label: Label)

    fun emitSub(memoryLocation: MemoryLocation, value: Int)

    /**
     * To jump to some label that not yet exists or is external
     */
    fun emitJmp(labelName: String)
}

inline class Label(private val storage: LongStorage) {
    constructor(offset: Int, id: Int) : this(LongStorage(offset, id))

    val offset: Int
        get() = storage.first

    val id: Int
        get() = storage.second

    override fun toString(): String = "Label(offset=$offset, id=$id)"
}

class AssemblerException(message: String) : Exception(message)