package backend

sealed class MemoryLocation {
    abstract val presentableText: String
}

class Memory(val pointer: Long) : MemoryLocation() {
    override val presentableText: String
        get() = "0x${pointer.toString(16)}"
}

class Register(val name: String) : MemoryLocation() {
    override val presentableText: String
        get() = name
}