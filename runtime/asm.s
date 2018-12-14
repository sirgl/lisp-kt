main.S:
	.text
Lstr0:
	.asciz "Unexpected parameter count"
	.globl f
	.globl f_satellite
	.globl main__init
	.globl __entry__
f:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movabsq $2305843009213693994, %rax
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	addq $16, %rsp
	popq %rbp
	retq

f_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq $64, %rsp
	movq %rdi, %rdi
//save registers for call r__size
	pushq %rdi
	movq %rdi, %rdi
	callq r__size
	movq %rax, -8(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq $2305843009213693952, %rax
	movq %rax, -16(%rbp)
//save registers for call r__eq
	pushq %rdi
	movq -8(%rbp), %rdi
	movq -16(%rbp), %rsi
	callq r__eq
	movq %rax, -24(%rbp)
//restore registers for call r__eq
	popq %rdi
//finish handling call r__eq
//save registers for call r__untag
	pushq %rdi
	movq -24(%rbp), %rdi
	callq r__untag
	movq %rax, -32(%rbp)
//restore registers for call r__untag
	popq %rdi
//finish handling call r__untag
	cmpq $0, -32(%rbp)
	je L1
L0:
//save registers for call f
	pushq %rdi
	callq f
	movq %rax, -40(%rbp)
//restore registers for call f
	popq %rdi
//finish handling call f
	movq -40(%rbp), %rax
	addq $64, %rsp
	popq %rbp
	retq
L1:
	movq $Lstr0, -64(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -64(%rbp), %rdi
	callq r__createString
	movq %rax, -48(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -48(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -56(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq $64, %rsp
	movq f_satellite@GOTPCREL(%rip), %rax
	movq %rax, -64(%rbp)
//save registers for call r__tagFunction
	movq -64(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -16(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
//mem (-16(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -16(%rbp), %rax
	movq %rax, -8(%rbp)
//mem (-8(%rbp)) -> mem (-32(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -32(%rbp)
	movq $0, -40(%rbp)
//save registers for call r__untag
	movq -32(%rbp), %rdi
	callq r__untag
	movq %rax, -48(%rbp)
//restore registers for call r__untag
//finish handling call r__untag
//save registers for call by ptr
	movq -40(%rbp), %rdi
	movq -48(%rbp), %rax
	call *%rax
	movq %rax, -56(%rbp)
//restore registers for call by ptr
//finish handling call call by ptr
	movq -56(%rbp), %rax
	addq $64, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq $16, %rsp
	popq %rbp
	retq