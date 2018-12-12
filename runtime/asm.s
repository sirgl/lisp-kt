	.text
Lstr0:
	.asciz "Hello world!"
	.globl main__init
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	movq %rax, -8(%rbp)
	movq $Lstr0, -32(%rbp)
//save registers for call r__createString
	movq -32(%rbp), %rdi
	callq r__createString
	movq %rax, -16(%rbp)
//restore registers for call r__createString
//finish handling call r__createString
//save registers for call print
	movq -16(%rbp), %rdi
	callq r__print
	movq %rax, -24(%rbp)
//restore registers for call print
//finish handling call print
	movq -24(%rbp), %rax
	addq $32, %rsp
	popq %rbp
	retq