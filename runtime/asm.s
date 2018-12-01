.globl _main__init

_main__init:
	pushq	%rbp
    movq	%rsp, %rbp
    subq $8, %rsp
	movq	$0, -8(%rbp)
	movq	-8(%rbp), %rax
    movq $0, %rax
    addq $8, %rsp
	popq	%rbp
	retq