package backend.codegen

import backend.MemoryLocation
import java.lang.StringBuilder

class TextAssembler : AssemblerX64 {
    override fun writeFunction(writer: (FunctionX64Assembler) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class FunctionX64TextAssembler : FunctionX64Assembler {
    private val sb = StringBuilder()

    override fun emitMov(from: MemoryLocation, to: MemoryLocation) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun emitRet() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

}