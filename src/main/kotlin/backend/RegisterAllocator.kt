package backend

import lir.LirFunction

class MemoryMap(
    // outer list for each lir index of instruction
    // inner list for all virtual registers. It describes how virtual registers are mapped to real before the given instruction
    val virtualRegToReal: List<List<MemoryLocation>> // for each position of program
) {
    fun getBytesToAllocateOnStack() : Int {
        val map = virtualRegToReal.firstOrNull() ?: return 0
        return map.mapNotNull { it as? AddressWithOffset }
            .maxBy { it.offset }?.offset ?: 0
    }
}

interface RegisterAllocator {
    fun allocateRegisters(lirFunction: LirFunction) : MemoryMap
}