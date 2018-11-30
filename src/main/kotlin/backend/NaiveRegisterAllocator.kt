package backend

import backend.x64.Regs
import backend.x64.parameterRegisters
import lir.LirFunction



/**
 * Naive memory allocation. Everything on stack, except parameters. When emitting call instruction, they should be pushed on stack
 */
class NaiveRegisterAllocator : RegisterAllocator {
    override fun allocateRegisters(lirFunction: LirFunction): MemoryMap {
        val instructions = lirFunction.instructions
        val virtualRegistersCount = lirFunction.virtualRegistersCount

        // global register map (not changed)
        val registerMap = mutableListOf<MemoryLocation>()
        val parameterCount = lirFunction.parameterCount
        for (i in 0 until parameterCount) {
            registerMap.add(parameterRegisters[i])
        }

        var offset = 8
        for(i in parameterCount until virtualRegistersCount) {
            registerMap.add(AddressWithOffset(Regs.rsp, offset))
            offset += 8
        }
        return MemoryMap(instructions.map { registerMap })
    }
}