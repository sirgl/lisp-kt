package backend

sealed class MemoryLocation {
    abstract val presentableText: String
}

sealed class Address : MemoryLocation()

class AddressWithOffset(val register: Register, val offset: Int) : Address() {
    override val presentableText: String
        get() = "${register.name} + $offset"
}

data class AbsAddress(val pointer: Long) : Address() {
    override val presentableText: String
        get() = "0x${pointer.toString(16)}"
}

data class Register(val name: String) : MemoryLocation() {
    override val presentableText: String
        get() = name
}