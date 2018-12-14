package codegen

import FrontendTest
import InMemoryFileInfo
import backend.AssemblyBackend
import backend.StringArtifactBuilder
import backend.BackendConfiguration
import backend.NaiveRegisterAllocator
import backend.codegen.TextAssembler
import util.InMemorySource
import frontend.CompilerConfig
import withText
import kotlin.test.Test
import kotlin.test.assertEquals

class CodegenTest : FrontendTest(emptyList()) {
    @Test
    fun `test top level list`() {
        testCodegen("""
main.S:
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}-8, %rsp
	movq ${'$'}0, -8(%rsp)
	movq -8(%rsp), %rax
	addq ${'$'}-8, %rsp
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
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}-8, %rsp
	movq ${'$'}0, -8(%rsp)
	movq -8(%rsp), %rax
	addq ${'$'}-8, %rsp
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
	.globl main__init
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}48, %rsp
	movq r__print, %rax
	movq %rax, -8(%rbp)
	movq ${'$'}0, -16(%rbp)
	movabsq ${'$'}2305843009213693964, %rax
	movq %rax, -24(%rbp)
//save registers for call r__withElement
	movq -24(%rbp), %rdi
	movq -16(%rbp), %rsi
	callq r__withElement
	movq %rax, -32(%rbp)
//restore registers for call r__withElement
//finish handling call r__withElement
//save registers for call r__print
	movq -32(%rbp), %rdi
	callq r__print
	movq %rax, -40(%rbp)
//restore registers for call r__print
//finish handling call r__print
	movq -40(%rbp), %rax
	addq ${'$'}48, %rsp
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
foo:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}-8, %rsp
	movq ${'$'}0, -8(%rsp)
	movq -8(%rsp), %rax
	addq ${'$'}-8, %rsp
	popq %rbp
	retq

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}-8, %rsp
	movq foo, -8(%rsp)
	movq -8(%rsp), %rax
	addq ${'$'}-8, %rsp
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
print:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}-8, %rsp
	movq ${'$'}0, -8(%rsp)
	movq -8(%rsp), %rax
	addq ${'$'}-8, %rsp
	popq %rbp
	retq

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}-8, %rsp
L2:
	movq print, -8(%rsp)
	movq ${'$'}0, -16(%rsp)
	cmpq ${'$'}0, -16(%rsp)
	jne L1
L0:
	movq ${'$'}0, -24(%rsp)
;save registers for call print
	movq -24(%rsp), %rdi
	callq print
	movq %rax, -32(%rsp)
;restore registers for call print
;finish handling call print
	jmp L2
L1:
	movq ${'$'}0, -40(%rsp)
	movq -40(%rsp), %rax
	addq ${'$'}-8, %rsp
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
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}-8, %rsp
	movq ${'$'}0, -16(%rsp)
;mem (-16(%rsp)) -> mem (-8(%rsp)) move through temporary register
	movq -16(%rsp), %r10
	movq %r10, -8(%rsp)
	movq ${'$'}0, -32(%rsp)
;mem (-32(%rsp)) -> mem (-8(%rsp)) move through temporary register
	movq -32(%rsp), %r10
	movq %r10, -8(%rsp)
	movq -40(%rsp), %rax
	addq ${'$'}-8, %rsp
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
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}-8, %rsp
	movq ${'$'}0, -16(%rsp)
;mem (-16(%rsp)) -> mem (-8(%rsp)) move through temporary register
	movq -16(%rsp), %r10
	movq %r10, -8(%rsp)
;mem (-8(%rsp)) -> mem (-32(%rsp)) move through temporary register
	movq -8(%rsp), %r10
	movq %r10, -32(%rsp)
	movq -32(%rsp), %rax
	addq ${'$'}-8, %rsp
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
        """.trimIndent(), listOf(
                "main" withText "`(let (\"foo\" 12 2) x)"
        ))
    }


    @Test
    fun `test vararg`() {
        testCodegen("""
main.S:
foo:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}-8, %rsp
	movq ${'$'}0, -8(%rsp)
	movq -8(%rsp), %rax
	addq ${'$'}-8, %rsp
	popq %rbp
	retq

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}-8, %rsp
	movq foo, -8(%rsp)
	movq ${'$'}1, -16(%rsp)
	movq ${'$'}0, -24(%rsp)
	movq ${'$'}0, -32(%rsp)
;save registers for call r__withElement
	movq -32(%rsp), %rdi
	movq -24(%rsp), %rsi
	callq r__withElement
	movq %rax, -40(%rsp)
;restore registers for call r__withElement
;finish handling call r__withElement
	movq ${'$'}1, -48(%rsp)
;save registers for call r__withElement
	movq -48(%rsp), %rdi
	movq -40(%rsp), %rsi
	callq r__withElement
	movq %rax, -56(%rsp)
;restore registers for call r__withElement
;finish handling call r__withElement
;save registers for call foo
	movq -16(%rsp), %rdi
	movq -56(%rsp), %rsi
	callq foo
	movq %rax, -64(%rsp)
;restore registers for call foo
;finish handling call foo
	movq -64(%rsp), %rax
	addq ${'$'}-8, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "(defn foo (a @vp) ())(foo 1 2 3)"
        ))
    }


    @Test
    fun `test call by variable`() {
        testCodegen("""
main:
fun foo params: 0, totalVars: 0
b0:
  load_const ()
  return b0:i0

fun main__init params: 0, totalVars: 1 (main)
b0:
  get_function_reference 0
  store_var: v0 value: b0:i0
  load_var: v0
  call_by_reference referneceInstr: b0:i2 args: ()
  return b0:i3
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
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}32, %rsp
	movq r__print, %rax
	movq %rax, -8(%rsp)
	movabsq ${'$'}2305843009213693964, %rax
	movq %rax, -16(%rsp)
//save registers for call r__print
	movq -16(%rsp), %rdi
	callq r__print
	movq %rax, -24(%rsp)
//restore registers for call r__print
//finish handling call r__print
	movq -24(%rsp), %rax
	addq ${'$'}32, %rsp
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
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}16, %rsp
	movabsq Lstr0, -16(%rbp)
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
	.asciz "Hello world!"
	.globl main__init
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}32, %rsp
	movq r__print, %rax
	movq %rax, -8(%rbp)
	movq ${'$'}Lstr0, -32(%rbp)
//save registers for call r__createString
	movq -32(%rbp), %rdi
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
	addq ${'$'}32, %rsp
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
	.asciz "Hello world!"
	.globl main__init
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}32, %rsp
	movq r__print, %rax
	movq %rax, -8(%rbp)
	movq ${'$'}Lstr0, -32(%rbp)
//save registers for call r__createString
	movq -32(%rbp), %rdi
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
	addq ${'$'}32, %rsp
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
                (while (lt i 10)
                    (print i)
                    (print "\n")
                    (set i (+ i 1))
                )
                i
            )

                """.trimIndent()
        ))
    }

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