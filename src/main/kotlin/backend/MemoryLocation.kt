package backend

sealed class MemoryLocation {
    abstract val presentableText: String

    abstract val assemblyText: String
}

sealed class Address : MemoryLocation()

class AddressWithOffset(val register: Register, val offset: Int) : Address() {
    override val assemblyText: String
        get() = "$offset(%${register.name})"
    override val presentableText: String
        get() = "${register.name} + $offset"
}

class AbsAddress(val pointer: Long) : Address() {
    override val assemblyText: String
        get() = presentableText
    override val presentableText: String
        get() = "0x${pointer.toString(16)}"
}

class Register(val name: String) : MemoryLocation() {
    override val assemblyText: String
        get() = "%$name"
    override val presentableText: String
        get() = name
}