	.text
	.globl main__init
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq $128, %rsp
	movq r__print, %rax
	movq %rax, -8(%rbp)
	movq $0, -16(%rbp)
	movq $0, -24(%rbp)
//save registers for call r__withElement
	movq -16(%rbp), %rdi
	movq -24(%rbp), %rsi
	callq r__withElement
	movq %rax, -32(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
	movq $0, -40(%rbp)
	movabsq $2305843009213693996, %rax
	movq %rax, -48(%rbp)
//save registers for call r__withElement
	movq -40(%rbp), %rdi
	movq -48(%rbp), %rsi
	callq r__withElement
	movq %rax, -56(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
	movabsq $2305843009213694075, %rax
	movq %rax, -64(%rbp)
//save registers for call r__withElement
	movq -56(%rbp), %rdi
	movq -64(%rbp), %rsi
	callq r__withElement
	movq %rax, -72(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
//save registers for call r__withElement
	movq -32(%rbp), %rdi
	movq -72(%rbp), %rsi
	callq r__withElement
	movq %rax, -80(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
	movabsq $2305843009213693974, %rax
	movq %rax, -88(%rbp)
//save registers for call r__withElement
	movq -80(%rbp), %rdi
	movq -88(%rbp), %rsi
	callq r__withElement
	movq %rax, -96(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
	movabsq $2305843009213693962, %rax
	movq %rax, -104(%rbp)
//save registers for call r__withElement
	movq -96(%rbp), %rdi
	movq -104(%rbp), %rsi
	callq r__withElement
	movq %rax, -112(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
//save registers for call r__print
	movq -112(%rbp), %rdi
	callq r__print
	movq %rax, -120(%rbp)
//restore registers for call r__print
//finish handling call r__print
	movq -120(%rbp), %rax
	addq $128, %rsp
	popq %rbp
	retq