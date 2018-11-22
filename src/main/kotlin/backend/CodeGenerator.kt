package backend

import backend.codegen.Assembler
import backend.codegen.FunctionAssembler
import backend.x64.Regs
import lir.*

class CodeGenerator(val fileAssembler: Assembler, val registerAllocator: RegisterAllocator) {
    fun generateCode(file: LirFile) {
        for (function in file.functions) {
            val registerMap = registerAllocator.allocateRegisters(function)
            fileAssembler.writeFunction("") {asm ->
                with(FunctionGenerationSession(function, registerMap)) {
                    asm.generate()
                }
            }
        }
    }


}

private class FunctionGenerationSession(val function: LirFunction, val memoryMap: MemoryMap) {
    fun FunctionAssembler.generate() {
        // TODO preamble ?
        val bytesToAllocateOnStack = memoryMap.getBytesToAllocateOnStack()
        emitSub(Regs.rsp, bytesToAllocateOnStack)
        for ((index, instruction) in function.instructions.withIndex()) {
            val instrRegisterMap = memoryMap.virtualRegToReal[index]
            when (instruction) {
                is LirMovInstr -> {
                    emitMov(instrRegisterMap[instruction.from]!!, instrRegisterMap[instruction.to]!!)
                }
                is LirBinInstr -> TODO()
                is LirGetFunctionPtrInstr -> TODO()
                is LirGetStrPtrInstr -> TODO()
                is LirCallInstr -> TODO()
                is LirCondJumpInstr -> TODO()
                is LirReturnInstr -> TODO()
                is LirGotoInstr -> TODO()
                is LirInplaceI64 -> TODO()
                is LirLoadGlobalVar -> TODO()
            }
        }
        // TODO cleanup
    }
}