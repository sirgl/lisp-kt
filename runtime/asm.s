main.S:
	.text
Lstr0:
	.asciz "Unexpected parameter count"
Lstr1:
	.asciz "\n"
	.globl m__plus_satellite
	.globl _gt_satellite
	.globl _eq_satellite
	.globl print_satellite
	.globl lt
	.globl lt_satellite
	.globl _ge
	.globl _ge_satellite
	.globl main__init
	.globl __entry__
m__plus_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq $112, %rsp
//mem (-8(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -8(%rbp)
//save registers for call r__size
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__size
	movq %rax, -16(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq $2305843009213693954, %rax
	movq %rax, -24(%rbp)
//save registers for call r__eq
	pushq %rdi
	movq -16(%rbp), %rdi
	movq -24(%rbp), %rsi
	callq r__eq
	movq %rax, -32(%rbp)
//restore registers for call r__eq
	popq %rdi
//finish handling call r__eq
//save registers for call r__untag
	pushq %rdi
	movq -32(%rbp), %rdi
	callq r__untag
	movq %rax, -40(%rbp)
//restore registers for call r__untag
	popq %rdi
//finish handling call r__untag
	cmpq $0, -40(%rbp)
	je L1
L0:
//save registers for call r__first
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__first
	movq %rax, -48(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__tail
	movq %rax, -56(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__first
	pushq %rdi
	movq -56(%rbp), %rdi
	callq r__first
	movq %rax, -64(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq -56(%rbp), %rdi
	callq r__tail
	movq %rax, -72(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__add
	pushq %rdi
	movq -48(%rbp), %rdi
	movq -64(%rbp), %rsi
	callq r__add
	movq %rax, -80(%rbp)
//restore registers for call r__add
	popq %rdi
//finish handling call r__add
	movq -80(%rbp), %rax
L1:
	movq $Lstr0, -104(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -104(%rbp), %rdi
	callq r__createString
	movq %rax, -88(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -88(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -96(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit
	addq $112, %rsp
	popq %rbp
	retq

_gt_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq $112, %rsp
//mem (-8(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -8(%rbp)
//save registers for call r__size
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__size
	movq %rax, -16(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq $2305843009213693954, %rax
	movq %rax, -24(%rbp)
//save registers for call r__eq
	pushq %rdi
	movq -16(%rbp), %rdi
	movq -24(%rbp), %rsi
	callq r__eq
	movq %rax, -32(%rbp)
//restore registers for call r__eq
	popq %rdi
//finish handling call r__eq
//save registers for call r__untag
	pushq %rdi
	movq -32(%rbp), %rdi
	callq r__untag
	movq %rax, -40(%rbp)
//restore registers for call r__untag
	popq %rdi
//finish handling call r__untag
	cmpq $0, -40(%rbp)
	je L3
L2:
//save registers for call r__first
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__first
	movq %rax, -48(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__tail
	movq %rax, -56(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__first
	pushq %rdi
	movq -56(%rbp), %rdi
	callq r__first
	movq %rax, -64(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq -56(%rbp), %rdi
	callq r__tail
	movq %rax, -72(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__gt
	pushq %rdi
	movq -48(%rbp), %rdi
	movq -64(%rbp), %rsi
	callq r__gt
	movq %rax, -80(%rbp)
//restore registers for call r__gt
	popq %rdi
//finish handling call r__gt
	movq -80(%rbp), %rax
L3:
	movq $Lstr0, -104(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -104(%rbp), %rdi
	callq r__createString
	movq %rax, -88(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -88(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -96(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit
	addq $112, %rsp
	popq %rbp
	retq

_eq_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq $112, %rsp
//mem (-8(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -8(%rbp)
//save registers for call r__size
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__size
	movq %rax, -16(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq $2305843009213693954, %rax
	movq %rax, -24(%rbp)
//save registers for call r__eq
	pushq %rdi
	movq -16(%rbp), %rdi
	movq -24(%rbp), %rsi
	callq r__eq
	movq %rax, -32(%rbp)
//restore registers for call r__eq
	popq %rdi
//finish handling call r__eq
//save registers for call r__untag
	pushq %rdi
	movq -32(%rbp), %rdi
	callq r__untag
	movq %rax, -40(%rbp)
//restore registers for call r__untag
	popq %rdi
//finish handling call r__untag
	cmpq $0, -40(%rbp)
	je L5
L4:
//save registers for call r__first
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__first
	movq %rax, -48(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__tail
	movq %rax, -56(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__first
	pushq %rdi
	movq -56(%rbp), %rdi
	callq r__first
	movq %rax, -64(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq -56(%rbp), %rdi
	callq r__tail
	movq %rax, -72(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__eq
	pushq %rdi
	movq -48(%rbp), %rdi
	movq -64(%rbp), %rsi
	callq r__eq
	movq %rax, -80(%rbp)
//restore registers for call r__eq
	popq %rdi
//finish handling call r__eq
	movq -80(%rbp), %rax
L5:
	movq $Lstr0, -104(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -104(%rbp), %rdi
	callq r__createString
	movq %rax, -88(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -88(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -96(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit
	addq $112, %rsp
	popq %rbp
	retq

print_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq $96, %rsp
//mem (-8(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -8(%rbp)
//save registers for call r__size
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__size
	movq %rax, -16(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq $2305843009213693953, %rax
	movq %rax, -24(%rbp)
//save registers for call r__eq
	pushq %rdi
	movq -16(%rbp), %rdi
	movq -24(%rbp), %rsi
	callq r__eq
	movq %rax, -32(%rbp)
//restore registers for call r__eq
	popq %rdi
//finish handling call r__eq
//save registers for call r__untag
	pushq %rdi
	movq -32(%rbp), %rdi
	callq r__untag
	movq %rax, -40(%rbp)
//restore registers for call r__untag
	popq %rdi
//finish handling call r__untag
	cmpq $0, -40(%rbp)
	je L7
L6:
//save registers for call r__first
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__first
	movq %rax, -48(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__tail
	movq %rax, -56(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__print
	pushq %rdi
	movq -48(%rbp), %rdi
	callq r__print
	movq %rax, -64(%rbp)
//restore registers for call r__print
	popq %rdi
//finish handling call r__print
	movq -64(%rbp), %rax
L7:
	movq $Lstr0, -88(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -88(%rbp), %rdi
	callq r__createString
	movq %rax, -72(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -72(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -80(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit
	addq $96, %rsp
	popq %rbp
	retq

lt:
	pushq %rbp
	movq %rsp, %rbp
	subq $208, %rsp
	movq %rdi, -32(%rbp)
	movq %rsi, -40(%rbp)
//save registers for call r__gt
	pushq %rdi
	pushq %rsi
	movq -32(%rbp), %rdi
	movq -40(%rbp), %rsi
	callq r__gt
	movq %rax, -48(%rbp)
//restore registers for call r__gt
	popq %rsi
	popq %rdi
//finish handling call r__gt
//save registers for call r__untag
	pushq %rdi
	pushq %rsi
	movq -48(%rbp), %rdi
	callq r__untag
	movq %rax, -56(%rbp)
//restore registers for call r__untag
	popq %rsi
	popq %rdi
//finish handling call r__untag
	cmpq $0, -56(%rbp)
	je L9
L8:
	movabsq $4611686018427387904, %rax
	movq %rax, -64(%rbp)
//mem (-64(%rbp)) -> mem (-16(%rbp)) move through temporary register
	movq -64(%rbp), %rax
	movq %rax, -16(%rbp)
	jmp L10
L9:
	movabsq $4611686018427387905, %rax
	movq %rax, -80(%rbp)
//mem (-80(%rbp)) -> mem (-16(%rbp)) move through temporary register
	movq -80(%rbp), %rax
	movq %rax, -16(%rbp)
L10:
//mem (-16(%rbp)) -> mem (-96(%rbp)) move through temporary register
	movq -16(%rbp), %rax
	movq %rax, -96(%rbp)
//save registers for call r__untag
	pushq %rdi
	pushq %rsi
	movq -96(%rbp), %rdi
	callq r__untag
	movq %rax, -104(%rbp)
//restore registers for call r__untag
	popq %rsi
	popq %rdi
//finish handling call r__untag
	cmpq $0, -104(%rbp)
	je L12
L11:
	movq %rdi, -112(%rbp)
	movq %rsi, -120(%rbp)
//save registers for call r__eq
	pushq %rdi
	pushq %rsi
	movq -112(%rbp), %rdi
	movq -120(%rbp), %rsi
	callq r__eq
	movq %rax, -128(%rbp)
//restore registers for call r__eq
	popq %rsi
	popq %rdi
//finish handling call r__eq
//save registers for call r__untag
	pushq %rdi
	pushq %rsi
	movq -128(%rbp), %rdi
	callq r__untag
	movq %rax, -136(%rbp)
//restore registers for call r__untag
	popq %rsi
	popq %rdi
//finish handling call r__untag
	cmpq $0, -136(%rbp)
	je L14
L13:
	movabsq $4611686018427387904, %rax
	movq %rax, -144(%rbp)
//mem (-144(%rbp)) -> mem (-32(%rbp)) move through temporary register
	movq -144(%rbp), %rax
	movq %rax, -32(%rbp)
	jmp L15
L14:
	movabsq $4611686018427387905, %rax
	movq %rax, -160(%rbp)
//mem (-160(%rbp)) -> mem (-32(%rbp)) move through temporary register
	movq -160(%rbp), %rax
	movq %rax, -32(%rbp)
L15:
//mem (-32(%rbp)) -> mem (-176(%rbp)) move through temporary register
	movq -32(%rbp), %rax
	movq %rax, -176(%rbp)
//mem (-176(%rbp)) -> mem (-24(%rbp)) move through temporary register
	movq -176(%rbp), %rax
	movq %rax, -24(%rbp)
	jmp L16
L12:
	movabsq $4611686018427387904, %rax
	movq %rax, -192(%rbp)
//mem (-192(%rbp)) -> mem (-24(%rbp)) move through temporary register
	movq -192(%rbp), %rax
	movq %rax, -24(%rbp)
L16:
//mem (-24(%rbp)) -> mem (-208(%rbp)) move through temporary register
	movq -24(%rbp), %rax
	movq %rax, -208(%rbp)
	movq -208(%rbp), %rax
	addq $208, %rsp
	popq %rbp
	retq

lt_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq $112, %rsp
//mem (-8(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -8(%rbp)
//save registers for call r__size
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__size
	movq %rax, -16(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq $2305843009213693954, %rax
	movq %rax, -24(%rbp)
//save registers for call r__eq
	pushq %rdi
	movq -16(%rbp), %rdi
	movq -24(%rbp), %rsi
	callq r__eq
	movq %rax, -32(%rbp)
//restore registers for call r__eq
	popq %rdi
//finish handling call r__eq
//save registers for call r__untag
	pushq %rdi
	movq -32(%rbp), %rdi
	callq r__untag
	movq %rax, -40(%rbp)
//restore registers for call r__untag
	popq %rdi
//finish handling call r__untag
	cmpq $0, -40(%rbp)
	je L18
L17:
//save registers for call r__first
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__first
	movq %rax, -48(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__tail
	movq %rax, -56(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__first
	pushq %rdi
	movq -56(%rbp), %rdi
	callq r__first
	movq %rax, -64(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq -56(%rbp), %rdi
	callq r__tail
	movq %rax, -72(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call lt
	pushq %rdi
	movq -48(%rbp), %rdi
	movq -64(%rbp), %rsi
	callq lt
	movq %rax, -80(%rbp)
//restore registers for call lt
	popq %rdi
//finish handling call lt
	movq -80(%rbp), %rax
L18:
	movq $Lstr0, -104(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -104(%rbp), %rdi
	callq r__createString
	movq %rax, -88(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -88(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -96(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit
	addq $112, %rsp
	popq %rbp
	retq

_ge:
	pushq %rbp
	movq %rsp, %rbp
	subq $80, %rsp
	movq %rdi, -16(%rbp)
	movq %rsi, -24(%rbp)
//save registers for call lt
	pushq %rdi
	pushq %rsi
	movq -16(%rbp), %rdi
	movq -24(%rbp), %rsi
	callq lt
	movq %rax, -32(%rbp)
//restore registers for call lt
	popq %rsi
	popq %rdi
//finish handling call lt
//save registers for call r__untag
	pushq %rdi
	pushq %rsi
	movq -32(%rbp), %rdi
	callq r__untag
	movq %rax, -40(%rbp)
//restore registers for call r__untag
	popq %rsi
	popq %rdi
//finish handling call r__untag
	cmpq $0, -40(%rbp)
	je L20
L19:
	movabsq $4611686018427387904, %rax
	movq %rax, -48(%rbp)
//mem (-48(%rbp)) -> mem (-16(%rbp)) move through temporary register
	movq -48(%rbp), %rax
	movq %rax, -16(%rbp)
	jmp L21
L20:
	movabsq $4611686018427387905, %rax
	movq %rax, -64(%rbp)
//mem (-64(%rbp)) -> mem (-16(%rbp)) move through temporary register
	movq -64(%rbp), %rax
	movq %rax, -16(%rbp)
L21:
//mem (-16(%rbp)) -> mem (-80(%rbp)) move through temporary register
	movq -16(%rbp), %rax
	movq %rax, -80(%rbp)
	movq -80(%rbp), %rax
	addq $80, %rsp
	popq %rbp
	retq

_ge_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq $112, %rsp
//mem (-8(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -8(%rbp)
//save registers for call r__size
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__size
	movq %rax, -16(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq $2305843009213693954, %rax
	movq %rax, -24(%rbp)
//save registers for call r__eq
	pushq %rdi
	movq -16(%rbp), %rdi
	movq -24(%rbp), %rsi
	callq r__eq
	movq %rax, -32(%rbp)
//restore registers for call r__eq
	popq %rdi
//finish handling call r__eq
//save registers for call r__untag
	pushq %rdi
	movq -32(%rbp), %rdi
	callq r__untag
	movq %rax, -40(%rbp)
//restore registers for call r__untag
	popq %rdi
//finish handling call r__untag
	cmpq $0, -40(%rbp)
	je L23
L22:
//save registers for call r__first
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__first
	movq %rax, -48(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq -8(%rbp), %rdi
	callq r__tail
	movq %rax, -56(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__first
	pushq %rdi
	movq -56(%rbp), %rdi
	callq r__first
	movq %rax, -64(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq -56(%rbp), %rdi
	callq r__tail
	movq %rax, -72(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call _ge
	pushq %rdi
	movq -48(%rbp), %rdi
	movq -64(%rbp), %rsi
	callq _ge
	movq %rax, -80(%rbp)
//restore registers for call _ge
	popq %rdi
//finish handling call _ge
	movq -80(%rbp), %rax
L23:
	movq $Lstr0, -104(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -104(%rbp), %rdi
	callq r__createString
	movq %rax, -88(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -88(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -96(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit
	addq $112, %rsp
	popq %rbp
	retq

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq $240, %rsp
	movq m__plus_satellite, %rax
	movq %rax, -192(%rbp)
//save registers for call r__tagFunction
	movq -192(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -16(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movq _gt_satellite, %rax
	movq %rax, -200(%rbp)
//save registers for call r__tagFunction
	movq -200(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -24(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movq _eq_satellite, %rax
	movq %rax, -208(%rbp)
//save registers for call r__tagFunction
	movq -208(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -32(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movq print_satellite, %rax
	movq %rax, -216(%rbp)
//save registers for call r__tagFunction
	movq -216(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -40(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movq lt_satellite, %rax
	movq %rax, -224(%rbp)
//save registers for call r__tagFunction
	movq -224(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -48(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movq _ge_satellite, %rax
	movq %rax, -232(%rbp)
//save registers for call r__tagFunction
	movq -232(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -56(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movabsq $2305843009213693952, %rax
	movq %rax, -64(%rbp)
//mem (-64(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -64(%rbp), %rax
	movq %rax, -8(%rbp)
L26:
//mem (-8(%rbp)) -> mem (-80(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -80(%rbp)
	movabsq $2305843009213693962, %rax
	movq %rax, -88(%rbp)
//save registers for call lt
	movq -80(%rbp), %rdi
	movq -88(%rbp), %rsi
	callq lt
	movq %rax, -96(%rbp)
//restore registers for call lt
//finish handling call lt
//save registers for call r__untag
	movq -96(%rbp), %rdi
	callq r__untag
	movq %rax, -104(%rbp)
//restore registers for call r__untag
//finish handling call r__untag
	cmpq $0, -104(%rbp)
	je L25
L24:
//mem (-8(%rbp)) -> mem (-112(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -112(%rbp)
//save registers for call r__print
	movq -112(%rbp), %rdi
	callq r__print
	movq %rax, -120(%rbp)
//restore registers for call r__print
//finish handling call r__print
	movq $Lstr1, -240(%rbp)
//save registers for call r__createString
	movq -240(%rbp), %rdi
	callq r__createString
	movq %rax, -128(%rbp)
//restore registers for call r__createString
//finish handling call r__createString
//save registers for call r__print
	movq -128(%rbp), %rdi
	callq r__print
	movq %rax, -136(%rbp)
//restore registers for call r__print
//finish handling call r__print
//mem (-8(%rbp)) -> mem (-144(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -144(%rbp)
	movabsq $2305843009213693953, %rax
	movq %rax, -152(%rbp)
//save registers for call r__add
	movq -144(%rbp), %rdi
	movq -152(%rbp), %rsi
	callq r__add
	movq %rax, -160(%rbp)
//restore registers for call r__add
//finish handling call r__add
//mem (-160(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -160(%rbp), %rax
	movq %rax, -8(%rbp)
	jmp L26
L25:
	movq $0, -176(%rbp)
//mem (-8(%rbp)) -> mem (-184(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -184(%rbp)
	movq -184(%rbp), %rax
	addq $240, %rsp
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
	addq $16, %rsp
	popq %rbp
	retq