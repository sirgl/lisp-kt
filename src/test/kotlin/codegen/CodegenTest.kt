package codegen

import FrontendTest
import InMemoryFileInfo
import backend.AssemblyBackend
import backend.BackendConfiguration
import backend.NaiveRegisterAllocator
import backend.StringArtifactBuilder
import backend.codegen.TextAssembler
import frontend.CompilerConfig
import util.InMemorySource
import withText
import kotlin.test.Test
import kotlin.test.assertEquals

class CodegenTest : FrontendTest(emptyList()) {
    @Test
    fun `test top level list`() {
        testCodegen("""
main.S:
	.text
	.globl main__init
	.globl __entry__
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
	movq ${'$'}0, -8(%rbp)
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "()"
        ))
    }
    @Test
    fun `test call simple`() {
        testCodegen("""
main.S:
	.text
Lstr0:
	.asciz "Unexpected parameter count"
	.globl print_satellite
	.globl main__init
	.globl __entry__
print_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}80, %rsp
	movq %rdi, %rdi
//save registers for call r__size
	pushq %rdi
	movq %rdi, %rdi
	callq r__size
	movq %rax, -8(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq ${'$'}2305843009213693953, %rax
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
	cmpq ${'$'}0, -32(%rbp)
	je L1
L0:
//save registers for call r__first
	pushq %rdi
	movq %rdi, %rdi
	callq r__first
	movq %rax, -40(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq %rdi, %rdi
	callq r__tail
	movq %rax, -48(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__print
	pushq %rdi
	movq -40(%rbp), %rdi
	callq r__print
	movq %rax, -56(%rbp)
//restore registers for call r__print
	popq %rdi
//finish handling call r__print
	movq -56(%rbp), %rax
	addq ${'$'}80, %rsp
	popq %rbp
	retq
L1:
	movq ${'$'}Lstr0, -80(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -80(%rbp), %rdi
	callq r__createString
	movq %rax, -64(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -64(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -72(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}32, %rsp
	movq print_satellite@GOTPCREL(%rip), %rax
	movq %rax, -32(%rbp)
//save registers for call r__tagFunction
	movq -32(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -8(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movabsq ${'$'}2305843009213693964, %rax
	movq %rax, -16(%rbp)
//save registers for call r__print
	movq -16(%rbp), %rdi
	callq r__print
	movq %rax, -24(%rbp)
//restore registers for call r__print
//finish handling call r__print
	movq -24(%rbp), %rax
	addq ${'$'}32, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "(defnat print r__print (x))(print 12)"
        ))
    }

    @Test
    fun `test print list with value`() {
        testCodegen("""
main.S:
	.text
Lstr0:
	.asciz "Unexpected parameter count"
	.globl print_satellite
	.globl main__init
	.globl __entry__
print_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}80, %rsp
	movq %rdi, %rdi
//save registers for call r__size
	pushq %rdi
	movq %rdi, %rdi
	callq r__size
	movq %rax, -8(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq ${'$'}2305843009213693953, %rax
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
	cmpq ${'$'}0, -32(%rbp)
	je L1
L0:
//save registers for call r__first
	pushq %rdi
	movq %rdi, %rdi
	callq r__first
	movq %rax, -40(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq %rdi, %rdi
	callq r__tail
	movq %rax, -48(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__print
	pushq %rdi
	movq -40(%rbp), %rdi
	callq r__print
	movq %rax, -56(%rbp)
//restore registers for call r__print
	popq %rdi
//finish handling call r__print
	movq -56(%rbp), %rax
	addq ${'$'}80, %rsp
	popq %rbp
	retq
L1:
	movq ${'$'}Lstr0, -80(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -80(%rbp), %rdi
	callq r__createString
	movq %rax, -64(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -64(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -72(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}32, %rsp
	movq print_satellite@GOTPCREL(%rip), %rax
	movq %rax, -32(%rbp)
//save registers for call r__tagFunction
	movq -32(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -8(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movabsq ${'$'}4611686018427387905, %rax
	movq %rax, -16(%rbp)
//save registers for call r__print
	movq -16(%rbp), %rdi
	callq r__print
	movq %rax, -24(%rbp)
//restore registers for call r__print
//finish handling call r__print
	movq -24(%rbp), %rax
	addq ${'$'}32, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "(defnat print r__print (x))(print #t)"
        ))
    }

    @Test
    fun `test function definition`() {
        testCodegen("""
main.S:
	.text
Lstr0:
	.asciz "Unexpected parameter count"
	.globl foo
	.globl foo_satellite
	.globl main__init
	.globl __entry__
foo:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
	movabsq ${'$'}2305843009213693994, %rax
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq

foo_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}80, %rsp
	movq %rdi, %rdi
//save registers for call r__size
	pushq %rdi
	movq %rdi, %rdi
	callq r__size
	movq %rax, -8(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq ${'$'}2305843009213693953, %rax
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
	cmpq ${'$'}0, -32(%rbp)
	je L1
L0:
//save registers for call r__first
	pushq %rdi
	movq %rdi, %rdi
	callq r__first
	movq %rax, -40(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq %rdi, %rdi
	callq r__tail
	movq %rax, -48(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call foo
	pushq %rdi
	movq -40(%rbp), %rdi
	callq foo
	movq %rax, -56(%rbp)
//restore registers for call foo
	popq %rdi
//finish handling call foo
	movq -56(%rbp), %rax
	addq ${'$'}80, %rsp
	popq %rbp
	retq
L1:
	movq ${'$'}Lstr0, -80(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -80(%rbp), %rdi
	callq r__createString
	movq %rax, -64(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -64(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -72(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
	movq foo_satellite@GOTPCREL(%rip), %rax
	movq %rax, -16(%rbp)
//save registers for call r__tagFunction
	movq -16(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -8(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "(defn foo(x) 42)"
        ))
    }

    @Test
    fun `test if`() {
        testCodegen("""
main.S:
	.text
	.globl main__init
	.globl __entry__
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}64, %rsp
	movabsq ${'$'}4611686018427387905, %rax
	movq %rax, -16(%rbp)
//save registers for call r__untag
	movq -16(%rbp), %rdi
	callq r__untag
	movq %rax, -24(%rbp)
//restore registers for call r__untag
//finish handling call r__untag
	cmpq ${'$'}0, -24(%rbp)
	je L1
L0:
	movabsq ${'$'}2305843009213693953, %rax
	movq %rax, -32(%rbp)
//mem (-32(%rbp)) -> mem (-16(%rbp)) move through temporary register
	movq -32(%rbp), %rax
	movq %rax, -16(%rbp)
	jmp L2
L1:
	movabsq ${'$'}2305843009213693954, %rax
	movq %rax, -48(%rbp)
//mem (-48(%rbp)) -> mem (-16(%rbp)) move through temporary register
	movq -48(%rbp), %rax
	movq %rax, -16(%rbp)
L2:
//mem (-16(%rbp)) -> mem (-64(%rbp)) move through temporary register
	movq -16(%rbp), %rax
	movq %rax, -64(%rbp)
	movq -64(%rbp), %rax
	addq ${'$'}64, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
""".trimIndent(), listOf(
                "main" withText "(if #t 1 2)"
        ))
    }

    @Test
    fun `test if nested`() {
        testCodegen("""
main.S:
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}-8, %rsp
	movq ${'$'}0, -24(%rsp)
	cmpq ${'$'}0, -24(%rsp)
	jne L1
L0:
	movq ${'$'}1, -32(%rsp)
;mem (-32(%rsp)) -> mem (-16(%rsp)) move through temporary register
	movq -32(%rsp), %r10
	movq %r10, -16(%rsp)
	jmp L2
L1:
	movq ${'$'}0, -48(%rsp)
	cmpq ${'$'}0, -48(%rsp)
	jne L4
L3:
	movq ${'$'}0, -56(%rsp)
;mem (-56(%rsp)) -> mem (-24(%rsp)) move through temporary register
	movq -56(%rsp), %r10
	movq %r10, -24(%rsp)
	jmp L5
L4:
	movq ${'$'}1, -72(%rsp)
;mem (-72(%rsp)) -> mem (-24(%rsp)) move through temporary register
	movq -72(%rsp), %r10
	movq %r10, -24(%rsp)
L5:
;mem (-24(%rsp)) -> mem (-88(%rsp)) move through temporary register
	movq -24(%rsp), %r10
	movq %r10, -88(%rsp)
;mem (-88(%rsp)) -> mem (-16(%rsp)) move through temporary register
	movq -88(%rsp), %r10
	movq %r10, -16(%rsp)
L2:
;mem (-16(%rsp)) -> mem (-104(%rsp)) move through temporary register
	movq -16(%rsp), %r10
	movq %r10, -104(%rsp)
	movq -104(%rsp), %rax
	addq ${'$'}-8, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText """
            (defnat + r__add (a b))
            (defnat _gt r__gt (a b))
            (macro _and (a b) (if a b #f))
            (defnat _eq r__eq (a b))
            (macro not (a) `(if a #f #t))
            (defn < (a b) (_and
                    (not (_gt a b))
                    (not (_eq a b))
                )
            )
            (let ((i 0))
                (while (< i 10)
                    (set i (+ i 1))
                )
                i
            )
                """.trimIndent()
        ))
    }

    @Test
    fun `test while`() {
        testCodegen("""
main.S:
	.text
Lstr0:
	.asciz "Unexpected parameter count"
	.globl print
	.globl print_satellite
	.globl main__init
	.globl __entry__
print:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
	movq ${'$'}0, -8(%rbp)
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq

print_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}80, %rsp
	movq %rdi, %rdi
//save registers for call r__size
	pushq %rdi
	movq %rdi, %rdi
	callq r__size
	movq %rax, -8(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq ${'$'}2305843009213693953, %rax
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
	cmpq ${'$'}0, -32(%rbp)
	je L1
L0:
//save registers for call r__first
	pushq %rdi
	movq %rdi, %rdi
	callq r__first
	movq %rax, -40(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq %rdi, %rdi
	callq r__tail
	movq %rax, -48(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call print
	pushq %rdi
	movq -40(%rbp), %rdi
	callq print
	movq %rax, -56(%rbp)
//restore registers for call print
	popq %rdi
//finish handling call print
	movq -56(%rbp), %rax
	addq ${'$'}80, %rsp
	popq %rbp
	retq
L1:
	movq ${'$'}Lstr0, -80(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -80(%rbp), %rdi
	callq r__createString
	movq %rax, -64(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -64(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -72(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}64, %rsp
	movq print_satellite@GOTPCREL(%rip), %rax
	movq %rax, -56(%rbp)
//save registers for call r__tagFunction
	movq -56(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -8(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
L4:
	movabsq ${'$'}4611686018427387905, %rax
	movq %rax, -16(%rbp)
//save registers for call r__untag
	movq -16(%rbp), %rdi
	callq r__untag
	movq %rax, -24(%rbp)
//restore registers for call r__untag
//finish handling call r__untag
	cmpq ${'$'}0, -24(%rbp)
	je L3
L2:
	movabsq ${'$'}2305843009213693994, %rax
	movq %rax, -32(%rbp)
//save registers for call print
	movq -32(%rbp), %rdi
	callq print
	movq %rax, -40(%rbp)
//restore registers for call print
//finish handling call print
	jmp L4
L3:
	movq ${'$'}0, -48(%rbp)
	movq -48(%rbp), %rax
	addq ${'$'}64, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "(defn print (x) ())(while #t (print 42))"
        ))
    }

    @Test
    fun `test assign`() {
        testCodegen("""
main.S:
	.text
	.globl main__init
	.globl __entry__
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}48, %rsp
	movabsq ${'$'}2305843009213693952, %rax
	movq %rax, -16(%rbp)
//mem (-16(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -16(%rbp), %rax
	movq %rax, -8(%rbp)
	movabsq ${'$'}2305843009213693964, %rax
	movq %rax, -32(%rbp)
//mem (-32(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -32(%rbp), %rax
	movq %rax, -8(%rbp)
	movq -40(%rbp), %rax
	addq ${'$'}48, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "(let (( y 0)) (set y 12))"
        ))
    }

    @Test
    fun `test let`() {
        testCodegen("""
main.S:
	.text
	.globl main__init
	.globl __entry__
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}32, %rsp
	movabsq ${'$'}2305843009213693964, %rax
	movq %rax, -16(%rbp)
//mem (-16(%rbp)) -> mem (-8(%rbp)) move through temporary register
	movq -16(%rbp), %rax
	movq %rax, -8(%rbp)
//mem (-8(%rbp)) -> mem (-32(%rbp)) move through temporary register
	movq -8(%rbp), %rax
	movq %rax, -32(%rbp)
	movq -32(%rbp), %rax
	addq ${'$'}32, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "(let ((x 12)) x)"
        ))
    }

    @Test
    fun `test data`() {
        testCodegen("""
main.S:
	.text
Lstr0:
	.asciz "x"
Lstr1:
	.asciz "foo"
Lstr2:
	.asciz "let"
	.globl main__init
	.globl __entry__
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}128, %rsp
	movq ${'$'}0, -8(%rbp)
	movq ${'$'}Lstr0, -112(%rbp)
//save registers for call r__createSymbol
	movq -112(%rbp), %rdi
	callq r__createSymbol
	movq %rax, -16(%rbp)
//restore registers for call r__createSymbol
//finish handling call r__createSymbol
//save registers for call r__withElement
	movq -8(%rbp), %rdi
	movq -16(%rbp), %rsi
	callq r__withElement
	movq %rax, -24(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
	movq ${'$'}0, -32(%rbp)
	movabsq ${'$'}2305843009213693954, %rax
	movq %rax, -40(%rbp)
//save registers for call r__withElement
	movq -32(%rbp), %rdi
	movq -40(%rbp), %rsi
	callq r__withElement
	movq %rax, -48(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
	movabsq ${'$'}2305843009213693964, %rax
	movq %rax, -56(%rbp)
//save registers for call r__withElement
	movq -48(%rbp), %rdi
	movq -56(%rbp), %rsi
	callq r__withElement
	movq %rax, -64(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
	movq ${'$'}Lstr1, -120(%rbp)
//save registers for call r__createString
	movq -120(%rbp), %rdi
	callq r__createString
	movq %rax, -72(%rbp)
//restore registers for call r__createString
//finish handling call r__createString
//save registers for call r__withElement
	movq -64(%rbp), %rdi
	movq -72(%rbp), %rsi
	callq r__withElement
	movq %rax, -80(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
//save registers for call r__withElement
	movq -24(%rbp), %rdi
	movq -80(%rbp), %rsi
	callq r__withElement
	movq %rax, -88(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
	movq ${'$'}Lstr2, -128(%rbp)
//save registers for call r__createSymbol
	movq -128(%rbp), %rdi
	callq r__createSymbol
	movq %rax, -96(%rbp)
//restore registers for call r__createSymbol
//finish handling call r__createSymbol
//save registers for call r__withElement
	movq -88(%rbp), %rdi
	movq -96(%rbp), %rsi
	callq r__withElement
	movq %rax, -104(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
	movq -104(%rbp), %rax
	addq ${'$'}128, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
""".trimIndent(), listOf(
                "main" withText "`(let (\"foo\" 12 2) x)"
        ))
    }


    @Test
    fun `test vararg`() {
        testCodegen("""
main.S:
	.text
Lstr0:
	.asciz "Unexpected parameter count"
	.globl foo
	.globl foo_satellite
	.globl main__init
	.globl __entry__
foo:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
	movq ${'$'}0, -8(%rbp)
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq

foo_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}80, %rsp
	movq %rdi, %rdi
//save registers for call r__size
	pushq %rdi
	movq %rdi, %rdi
	callq r__size
	movq %rax, -8(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq ${'$'}2305843009213693954, %rax
	movq %rax, -16(%rbp)
//save registers for call _ge
	pushq %rdi
	movq -8(%rbp), %rdi
	movq -16(%rbp), %rsi
	callq _ge
	movq %rax, -24(%rbp)
//restore registers for call _ge
	popq %rdi
//finish handling call _ge
//save registers for call r__untag
	pushq %rdi
	movq -24(%rbp), %rdi
	callq r__untag
	movq %rax, -32(%rbp)
//restore registers for call r__untag
	popq %rdi
//finish handling call r__untag
	cmpq ${'$'}0, -32(%rbp)
	je L1
L0:
//save registers for call r__first
	pushq %rdi
	movq %rdi, %rdi
	callq r__first
	movq %rax, -40(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq %rdi, %rdi
	callq r__tail
	movq %rax, -48(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call foo
	pushq %rdi
	movq -40(%rbp), %rdi
	movq -48(%rbp), %rsi
	callq foo
	movq %rax, -56(%rbp)
//restore registers for call foo
	popq %rdi
//finish handling call foo
	movq -56(%rbp), %rax
	addq ${'$'}80, %rsp
	popq %rbp
	retq
L1:
	movq ${'$'}Lstr0, -80(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -80(%rbp), %rdi
	callq r__createString
	movq %rax, -64(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -64(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -72(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}80, %rsp
	movq foo_satellite@GOTPCREL(%rip), %rax
	movq %rax, -72(%rbp)
//save registers for call r__tagFunction
	movq -72(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -8(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movabsq ${'$'}2305843009213693953, %rax
	movq %rax, -16(%rbp)
	movq ${'$'}0, -24(%rbp)
	movabsq ${'$'}2305843009213693954, %rax
	movq %rax, -32(%rbp)
//save registers for call r__withElement
	movq -24(%rbp), %rdi
	movq -32(%rbp), %rsi
	callq r__withElement
	movq %rax, -40(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
	movabsq ${'$'}2305843009213693955, %rax
	movq %rax, -48(%rbp)
//save registers for call r__withElement
	movq -40(%rbp), %rdi
	movq -48(%rbp), %rsi
	callq r__withElement
	movq %rax, -56(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
//save registers for call foo
	movq -16(%rbp), %rdi
	movq -56(%rbp), %rsi
	callq foo
	movq %rax, -64(%rbp)
//restore registers for call foo
//finish handling call foo
	movq -64(%rbp), %rax
	addq ${'$'}80, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "(defn foo (a @vp) ())(foo 1 2 3)"
        ))
    }


    @Test
    fun `test call by variable`() {
        testCodegen("""
main.S:
	.text
Lstr0:
	.asciz "Unexpected parameter count"
	.globl foo
	.globl foo_satellite
	.globl main__init
	.globl __entry__
foo:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
	movq ${'$'}0, -8(%rbp)
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq

foo_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}64, %rsp
	movq %rdi, %rdi
//save registers for call r__size
	pushq %rdi
	movq %rdi, %rdi
	callq r__size
	movq %rax, -8(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq ${'$'}2305843009213693952, %rax
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
	cmpq ${'$'}0, -32(%rbp)
	je L1
L0:
//save registers for call foo
	pushq %rdi
	callq foo
	movq %rax, -40(%rbp)
//restore registers for call foo
	popq %rdi
//finish handling call foo
	movq -40(%rbp), %rax
	addq ${'$'}64, %rsp
	popq %rbp
	retq
L1:
	movq ${'$'}Lstr0, -64(%rbp)
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
	subq ${'$'}64, %rsp
	movq foo_satellite@GOTPCREL(%rip), %rax
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
	movq ${'$'}0, -40(%rbp)
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
	addq ${'$'}64, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
            "main" withText """
        (let ((f (defn foo () ()))) (f))
                """.trimIndent()
        ))
    }


    @Test
    fun `test simple call`() {
        testCodegen("""
main.S:
	.text
Lstr0:
	.asciz "Unexpected parameter count"
Lstr1:
	.asciz "Hello"
	.globl print_satellite
	.globl main__init
	.globl __entry__
print_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}80, %rsp
	movq %rdi, %rdi
//save registers for call r__size
	pushq %rdi
	movq %rdi, %rdi
	callq r__size
	movq %rax, -8(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq ${'$'}2305843009213693953, %rax
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
	cmpq ${'$'}0, -32(%rbp)
	je L1
L0:
//save registers for call r__first
	pushq %rdi
	movq %rdi, %rdi
	callq r__first
	movq %rax, -40(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq %rdi, %rdi
	callq r__tail
	movq %rax, -48(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__print
	pushq %rdi
	movq -40(%rbp), %rdi
	callq r__print
	movq %rax, -56(%rbp)
//restore registers for call r__print
	popq %rdi
//finish handling call r__print
	movq -56(%rbp), %rax
	addq ${'$'}80, %rsp
	popq %rbp
	retq
L1:
	movq ${'$'}Lstr0, -80(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -80(%rbp), %rdi
	callq r__createString
	movq %rax, -64(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -64(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -72(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}48, %rsp
	movq print_satellite@GOTPCREL(%rip), %rax
	movq %rax, -32(%rbp)
//save registers for call r__tagFunction
	movq -32(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -8(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movq ${'$'}Lstr1, -40(%rbp)
//save registers for call r__createString
	movq -40(%rbp), %rdi
	callq r__createString
	movq %rax, -16(%rbp)
//restore registers for call r__createString
//finish handling call r__createString
//save registers for call r__print
	movq -16(%rbp), %rdi
	callq r__print
	movq %rax, -24(%rbp)
//restore registers for call r__print
//finish handling call r__print
	movq -24(%rbp), %rax
	addq ${'$'}48, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
            "main" withText """
        (defnat print r__print (x))
        (print "Hello")
                """.trimIndent()
        ))
    }


    @Test
    fun `test string literal`() {
        testCodegen("""
main.S:
	.text
Lstr0:
	.asciz "Hello world!"
	.globl main__init
	.globl __entry__
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
	movq ${'$'}Lstr0, -16(%rbp)
//save registers for call r__createString
	movq -16(%rbp), %rdi
	callq r__createString
	movq %rax, -8(%rbp)
//restore registers for call r__createString
//finish handling call r__createString
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """, listOf(
                "main" withText """
        "Hello world!"
                """.trimIndent()
        ))
    }

    @Test
    fun `test string literal inside print`() {
        testCodegen("""
main.S:
	.text
Lstr0:
	.asciz "Unexpected parameter count"
Lstr1:
	.asciz "Hello world!"
	.globl print_satellite
	.globl main__init
	.globl __entry__
print_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}80, %rsp
	movq %rdi, %rdi
//save registers for call r__size
	pushq %rdi
	movq %rdi, %rdi
	callq r__size
	movq %rax, -8(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq ${'$'}2305843009213693953, %rax
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
	cmpq ${'$'}0, -32(%rbp)
	je L1
L0:
//save registers for call r__first
	pushq %rdi
	movq %rdi, %rdi
	callq r__first
	movq %rax, -40(%rbp)
//restore registers for call r__first
	popq %rdi
//finish handling call r__first
//save registers for call r__tail
	pushq %rdi
	movq %rdi, %rdi
	callq r__tail
	movq %rax, -48(%rbp)
//restore registers for call r__tail
	popq %rdi
//finish handling call r__tail
//save registers for call r__print
	pushq %rdi
	movq -40(%rbp), %rdi
	callq r__print
	movq %rax, -56(%rbp)
//restore registers for call r__print
	popq %rdi
//finish handling call r__print
	movq -56(%rbp), %rax
	addq ${'$'}80, %rsp
	popq %rbp
	retq
L1:
	movq ${'$'}Lstr0, -80(%rbp)
//save registers for call r__createString
	pushq %rdi
	movq -80(%rbp), %rdi
	callq r__createString
	movq %rax, -64(%rbp)
//restore registers for call r__createString
	popq %rdi
//finish handling call r__createString
//save registers for call r__printErrorAndExit
	pushq %rdi
	movq -64(%rbp), %rdi
	callq r__printErrorAndExit
	movq %rax, -72(%rbp)
//restore registers for call r__printErrorAndExit
	popq %rdi
//finish handling call r__printErrorAndExit

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}48, %rsp
	movq print_satellite@GOTPCREL(%rip), %rax
	movq %rax, -32(%rbp)
//save registers for call r__tagFunction
	movq -32(%rbp), %rdi
	callq r__tagFunction
	movq %rax, -8(%rbp)
//restore registers for call r__tagFunction
//finish handling call r__tagFunction
	movq ${'$'}Lstr1, -40(%rbp)
//save registers for call r__createString
	movq -40(%rbp), %rdi
	callq r__createString
	movq %rax, -16(%rbp)
//restore registers for call r__createString
//finish handling call r__createString
//save registers for call r__print
	movq -16(%rbp), %rdi
	callq r__print
	movq %rax, -24(%rbp)
//restore registers for call r__print
//finish handling call r__print
	movq -24(%rbp), %rax
	addq ${'$'}48, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """, listOf(
                "main" withText """
        (defnat print r__print (x))
        (print "Hello world!")
                """.trimIndent()
        ))
    }
    @Test
    fun `test real`() {
        testCodegen("""
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
	subq ${'$'}16, %rsp
	movabsq ${'$'}2305843009213693994, %rax
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq

f_satellite:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}64, %rsp
	movq %rdi, %rdi
//save registers for call r__size
	pushq %rdi
	movq %rdi, %rdi
	callq r__size
	movq %rax, -8(%rbp)
//restore registers for call r__size
	popq %rdi
//finish handling call r__size
	movabsq ${'$'}2305843009213693952, %rax
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
	cmpq ${'$'}0, -32(%rbp)
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
	addq ${'$'}64, %rsp
	popq %rbp
	retq
L1:
	movq ${'$'}Lstr0, -64(%rbp)
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
	subq ${'$'}64, %rsp
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
	movq ${'$'}0, -40(%rbp)
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
	addq ${'$'}64, %rsp
	popq %rbp
	retq

__entry__:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
//save registers for call main__init
	callq main__init
	movq %rax, -8(%rbp)
//restore registers for call main__init
//finish handling call main__init
	movq -8(%rbp), %rax
	addq ${'$'}16, %rsp
	popq %rbp
	retq
        """, listOf(
                "main" withText """
            (defnat + r__add (a b))
            (defnat _gt r__gt (a b))
            (macro _and (a b) `(if a b #f))
            (defnat _eq r__eq (a b))
            (defnat print r__print (a))
            (macro not (a) `(if a #f #t))
            (defn lt (a b) (_and
                    (not (_gt a b))
                    (not (_eq a b))
                )
            )
            (defn _ge (a b) (not (lt a b)
                )
            )
            (let ((i 0))
                (while (lt i 50)
                    (print i)
                    (print "\n")
                    (set i (+ i 1))
                )
                i
            )

                """.trimIndent()
        ))
    }
//                (defnat + r__add (a b))
//            (defnat _gt r__gt (a b))
//            (macro _and (a b) `(if a b #f))
//            (defnat _eq r__eq (a b))
//            (defnat print r__print (a))
//            (macro not (a) `(if a #f #t))
//            (defn lt (a b) (_and
//                    (not (_gt a b))
//                    (not (_eq a b))
//                )
//            )
//            (defn _ge (a b) (not (lt a b)
//                )
//            )

    fun testCodegen(expected: String, files: List<InMemoryFileInfo>) {
        val sources = files.map { InMemorySource(it.text, it.name) }
        val session = frontend.compilationSession(sources, emptyList(), CompilerConfig(0), false)
        val lir = session.getLir().unwrap()

        val backend = AssemblyBackend(TextAssembler(), NaiveRegisterAllocator())
        val actual = lir.joinToString("\n") {
            val artifactBuilder = StringArtifactBuilder()
            backend.runBackend(BackendConfiguration(), it, artifactBuilder)
            artifactBuilder.sb.toString()
        }
        assertEquals(expected.trim(), actual.trim())
    }
}