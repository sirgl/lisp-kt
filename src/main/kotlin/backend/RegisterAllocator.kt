package backend

import lir.LirFunction
import kotlin.math.absoluteValue

class MemoryMap(
    // outer list for each lir index of instruction
    // inner list for all virtual registers. It describes how virtual registers are mapped to real before the given instruction
    val virtualRegToReal: List<List<MemoryLocation>> // for each position of program
) {
    fun getBytesToAllocateOnStack() : Int {
        val map = virtualRegToReal.firstOrNull() ?: return 0
        return map.mapNotNull { it as? AddressWithOffset }
            .maxBy { it.offset.absoluteValue }?.offset?.absoluteValue ?: 0
    }
}

interface RegisterAllocator {
    fun allocateRegisters(lirFunction: LirFunction) : MemoryMap
}