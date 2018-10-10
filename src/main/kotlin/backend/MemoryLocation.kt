package backend

sealed class MemoryLocation

class Memory(val pointer: Long) : MemoryLocation()

class Register(parentInfo: RegisterParentInfo?, val size: Int) : MemoryLocation()

class RegisterParentInfo(val parentRegister: Register, val offsetInParentBits: Int)