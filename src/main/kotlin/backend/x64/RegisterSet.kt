package backend.x64

import backend.Register
import backend.RegisterParentInfo

object X64Registers {
    private val registers = mutableListOf<Register>()

    private fun register(register: Register) : Register {
        registers.add(register)
        return register
    }

    private fun x64Register(name: String, parentInfo: RegisterParentInfo? = null) : Register {
        return register(Register(parentInfo, 64, name))
    }

    val rax = x64Register("rax")
    val rbx = x64Register("rbx")
    val rcx = x64Register("rcx")
    val rdx = x64Register("rdx")
    val rsi = x64Register("rsi")
    val rdi = x64Register("rdi")
    val rbp = x64Register("rbp")
    val rsp = x64Register("rsp")
}