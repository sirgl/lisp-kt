package backend.x64

/**
 * Special calling convention for LISP
 * count of arguments is placed in rdi
 * arguments (of size 64 bit) pushed on stack from left to right
 */
object LispCallingConvention : X64CallingConvention {
    override val name: String
        get() = "Lisp"
}