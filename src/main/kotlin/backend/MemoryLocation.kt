package backend

sealed class MemoryLocation {
    abstract val presentableText: String
}

class Memory(val pointer: Long) : MemoryLocation() {
    override val presentableText: String
        get() = "0x${pointer.toString(16)}"
}

class Register(parentInfo: RegisterParentInfo?, val size: Int, val name: String) : MemoryLocation() {
    override val presentableText: String
        get() = name
}

class RegisterParentInfo(val parentRegister: Register, val offsetInParentBits: Int)