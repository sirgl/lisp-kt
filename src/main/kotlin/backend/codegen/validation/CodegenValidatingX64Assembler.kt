package backend.codegen.validation

import backend.codegen.AssemblerX64
import backend.codegen.FunctionX64Assembler
import util.io.OutputStream



class CodegenValidatingX64Assembler : AssemblerX64 {
    override fun writeFunction(name: String, writer: (FunctionX64Assembler) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun save(outputStream: OutputStream) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}