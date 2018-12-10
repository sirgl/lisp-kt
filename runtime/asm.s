.globl main__init

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq $24, %rsp
	//movq r__print, -8(%rsp)
	movabsq $2305843009213693964, %rax
	movq %rax, -16(%rsp)
//save registers for call r__print
	movq -16(%rsp), %rdi
	callq r__print
	movq %rax, -24(%rsp)
//restore registers for call r__print
//finish handling call r__print
	movq -24(%rsp), %rax
	addq $24, %rsp
	popq %rbp
	retq