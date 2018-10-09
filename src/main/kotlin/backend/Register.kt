package backend

class Register(parentInfo: RegisterParentInfo?, val size: Int)

class RegisterParentInfo(val parentRegister: Register, val offsetInParentBits: Int)