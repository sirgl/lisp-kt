	.text
	.globl src_test_resources_integration_base_Main_lisp__init
	.globl __entry__
src_test_resources_integration_base_Main_lisp__init:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	movabsq $2305843009213693954, %rax
	movq %rax, -8(%rbp)
	movabsq $2305843009213693954, %rax
	movq %rax, -16(%rbp)
//save registers for call r__add
	movq -8(%rbp), %rdi
	movq -16(%rbp), %rsi
	callq r__add
	movq %rax, -24(%rbp)
//restore registers for call r__add
//finish handling call r__add
	movq -24(%rbp), %rax
	addq $32, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
//save registers for call src_main_resources_stdlib_lisp__init
	callq src_test_resources_integration_base_Main_lisp__init
	movq %rax, -8(%rbp)
//restore registers for call src_main_resources_stdlib_lisp__init
//finish handling call src_main_resources_stdlib_lisp__init
	movq -8(%rbp), %rax
	addq $16, %rsp
	popq %rbp
	retq
