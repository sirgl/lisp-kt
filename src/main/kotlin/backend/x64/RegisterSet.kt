package backend.x64

import backend.Register

object Regs {
    private val registers = mutableListOf<Register>()

    private fun register(register: Register) : Register {
        registers.add(register)
        return register
    }

    private fun x64Register(name: String) : Register {
        return register(Register(name))
    }

    val rax = x64Register("rax")
    val rbx = x64Register("rbx")
    val rcx = x64Register("rcx")
    val rdx = x64Register("rdx")
    val rsi = x64Register("rsi")
    val rdi = x64Register("rdi")
    val rbp = x64Register("rbp")
    val rsp = x64Register("rsp")
    val r8 = x64Register("r8")
    val r9 = x64Register("r9")
    val r10 = x64Register("r10")
}

// all of them are not preserved between function calls
val parameterRegisters = arrayOf(Regs.rdi, Regs.rsi, Regs.rdx, Regs.rcx, Regs.r8, Regs.r9)