package x64

object SystemVCallingConvention : X64CallingConvention {
    override val name: String
        get() = "System V"
}