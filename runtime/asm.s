.globl _main__init

_main__init:
    pushq %rbp
    movq %rsp, %rbp
    subq $-8, %rsp
    movq $0, -16(%rsp)
//save registers for call r__print
    movq -16(%rsp), %rdi
    callq _r__print
    movq %rax, -24(%rsp)
//restore registers for call r__print
//finish handling call r__print
    movq -24(%rsp), %rax
    addq $-8, %rsp
    popq %rbp
    retq