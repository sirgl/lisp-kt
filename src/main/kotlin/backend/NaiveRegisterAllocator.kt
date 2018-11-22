package backend

import backend.x64.Regs
import lir.LirFunction

// all of them are not preserved between function calls
private val parameterRegisters = arrayOf(Regs.rdi, Regs.rsi, Regs.rdx, Regs.rcx, Regs.r8, Regs.r9)

/**
 * Naive memory allocation. Everything on stack, except parameters. When emitting call instruction, they should be pushed on stack
 */
class NaiveRegisterAllocator : RegisterAllocator {
    override fun allocateRegisters(lirFunction: LirFunction): MemoryMap {
        val instructions = lirFunction.instructions
        val virtualRegistersCount = lirFunction.virtualRegistersCount

        // global register map (not changed)
        val registerMap = hashMapOf<Int, MemoryLocation>()
        val parameterCount = lirFunction.parameterCount
        for (i in 0 until parameterCount) {
            registerMap[i] = parameterRegisters[i]
        }

        var offset = 8
        for(i in parameterCount until virtualRegistersCount) {
            registerMap[i] = AddressWithOffset(Regs.rsp, 8)
            offset += 8
        }
        return MemoryMap(instructions.map { registerMap })
    }
}