package backend

import lir.LirFunction

class MemoryMap(
    val virtualRegToReal: List<Map<Int, MemoryLocation>> // for each position of program
) {
    fun getBytesToAllocateOnStack() : Int {
        val map = virtualRegToReal.firstOrNull() ?: return 0
        return map.mapNotNull { (_, value) -> (value as? AddressWithOffset) }
            .maxBy { it.offset }?.offset ?: 0
    }
}

interface RegisterAllocator {
    fun allocateRegisters(lirFunction: LirFunction) : MemoryMap
}