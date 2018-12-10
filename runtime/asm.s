	.text
	.globl main__init
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	movq r__print, %rax
	movq %rax, -8(%rbp)
	movabsq $2305843009213693964, %rax
	movq %rax, -16(%rbp)
//save registers for call r__print
	movq -16(%rbp), %rdi
	callq r__print
	movq %rax, -24(%rbp)
//restore registers for call r__print
//finish handling call r__print
	movq -24(%rbp), %rax
	addq $32, %rsp
	popq %rbp
	retq