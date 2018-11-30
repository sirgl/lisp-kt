package backend

import backend.codegen.Assembler
import backend.codegen.FunctionAssembler
import backend.x64.Regs
import backend.x64.parameterRegisters
import lir.*

class AssemblyBackend(
    private val fileAssembler: Assembler,
    private val registerAllocator: RegisterAllocator
) : Backend {
    override fun runBackend(
        config: BackendConfiguration,
        file: LirFile,
        artifactBuilder: ArtifactBuilder
    ) : List<Artifact> {
        fileAssembler.writeStringTable(file.stringTable)
        for (function in file.functions) {
            val registerMap = registerAllocator.allocateRegisters(function)
            fileAssembler.writeFunction(function.name) {asm ->
                with(FunctionGenerationSession(function, registerMap)) {
                    asm.generate()
                }
            }
        }
        artifactBuilder.createFileArtifact(file.source.path + ".S") { os ->
            fileAssembler.save(os)
        }
        return emptyList()
    }
}



private class FunctionGenerationSession(val function: LirFunction, val memoryMap: MemoryMap) {
    fun FunctionAssembler.generate() {
        val indexToLabel = getLabelPositions()
        val bytesToAllocateOnStack = memoryMap.getBytesToAllocateOnStack()
        emitPush(Regs.rbp)
        emitMov(Regs.rsp, Regs.rbp)
        emitSub(bytesToAllocateOnStack, Regs.rsp)
        for ((index, instruction) in function.instructions.withIndex()) {
            if (index in indexToLabel) {
                emitLabel(indexToLabel[index]!!)
            }
            val instrRegisterMap = memoryMap.virtualRegToReal[index]
            when (instruction) {
                is LirMovInstr -> {
                    emitMovSmart(instrRegisterMap[instruction.from], instrRegisterMap[instruction.to])
                }
                is LirBinInstr -> TODO()
                is LirGetFunctionPtrInstr -> {
                    emitMov(instruction.name, instrRegisterMap[instruction.destReg])
                }
                is LirGetStrPtrInstr -> {
                    emitMovabs("Lstr${instruction.strIndex}", instrRegisterMap[instruction.destReg])
                }
                is LirCallInstr -> {
                    emitComment("save registers for call ${instruction.functionName}")
                    for (i in 0 until function.parameterCount) {
                        emitPush(instrRegisterMap[i])
                    }
                    val realArgPositions = instruction.regArgs.map { instrRegisterMap[it] }
                    for ((argIndex, realArgPosition) in realArgPositions.withIndex()) {
                        emitMovSmart(realArgPosition, parameterRegisters[argIndex])
                    }
                    emitCall(instruction.functionName)
                    emitMovSmart(Regs.rax, instrRegisterMap[instruction.resultReg])
                    emitComment("restore registers for call ${instruction.functionName}")

                    // TODO check that all parameters popped
                    for (i in function.parameterCount - 1 downTo 0 step 1) {
                        emitPop(instrRegisterMap[i])
                    }
                    emitComment("finish handling call ${instruction.functionName}")
                }
                is LirCondJumpInstr -> {
                    emitCmpWithZero(instrRegisterMap[instruction.condReg])
                    emitJne(indexToLabel[instruction.elseInstrIndex]!!)
                }
                is LirReturnInstr -> {
                    emitMovSmart(instrRegisterMap[instruction.reg], Regs.rax)
                }
                is LirGotoInstr -> {
                    emitJmp(indexToLabel[instruction.instrIndex]!!)
                }
                is LirInplaceI64 -> {
                    emitMov(instruction.value, instrRegisterMap[instruction.register])
                }
                is LirLoadGlobalVar -> TODO()
            }
        }
        emitAdd(bytesToAllocateOnStack, Regs.rsp)
        emitPop(Regs.rbp)
        emitRet()
    }

    private fun getLabelPositions(): HashMap<Int, String> {
        var nextLabelIndex = 0
        val lirBBStartIndices = hashMapOf<Int, String>()
        for (instruction in function.instructions) {
            when (instruction) {
                is LirCondJumpInstr -> {
                    lirBBStartIndices[instruction.thenInstructionIndex] = "L$nextLabelIndex"
                    lirBBStartIndices[instruction.elseInstrIndex] = "L${nextLabelIndex + 1}"
                    nextLabelIndex += 2

                }
                is LirGotoInstr -> {
                    lirBBStartIndices[instruction.instrIndex] = "L$nextLabelIndex"
                    nextLabelIndex++
                }
            }
        }
        return lirBBStartIndices
    }

    private fun FunctionAssembler.emitMovSmart(from: MemoryLocation, to: MemoryLocation) {
        if (from !is Register && to !is Register) {
            emitComment("mem (${from.assemblyText}) -> mem (${to.assemblyText}) move through temporary register")
            emitMov(from, Regs.r10)
            emitMov(Regs.r10, to)
        } else {
            emitMov(from, to)
        }
    }
}