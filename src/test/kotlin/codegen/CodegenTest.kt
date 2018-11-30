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
	subq ${'$'}8, %rsp
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), %rax
	addq ${'$'}8, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "()"
        ))
    }

    @Test
    fun `test function definition`() {
        testCodegen("""
main.S:
foo:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}8, %rsp
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), %rax
	addq ${'$'}8, %rsp
	popq %rbp
	retq

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}8, %rsp
	movq foo, 8(%rsp)
	movq 8(%rsp), %rax
	addq ${'$'}8, %rsp
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
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}56, %rsp
	movq ${'$'}0, 16(%rsp)
	cmpq ${'$'}0, 16(%rsp)
	jne L1
L0:
	movq ${'$'}1, 24(%rsp)
;mem (24(%rsp)) -> mem (16(%rsp)) move through temporary register
	movq 24(%rsp), %r10
	movq %r10, 16(%rsp)
	jmp L2
L1:
	movq ${'$'}0, 40(%rsp)
;mem (40(%rsp)) -> mem (16(%rsp)) move through temporary register
	movq 40(%rsp), %r10
	movq %r10, 16(%rsp)
L2:
;mem (16(%rsp)) -> mem (56(%rsp)) move through temporary register
	movq 16(%rsp), %r10
	movq %r10, 56(%rsp)
	movq 56(%rsp), %rax
	addq ${'$'}56, %rsp
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
	subq ${'$'}104, %rsp
	movq ${'$'}0, 24(%rsp)
	cmpq ${'$'}0, 24(%rsp)
	jne L1
L0:
	movq ${'$'}1, 32(%rsp)
;mem (32(%rsp)) -> mem (16(%rsp)) move through temporary register
	movq 32(%rsp), %r10
	movq %r10, 16(%rsp)
	jmp L2
L1:
	movq ${'$'}0, 48(%rsp)
	cmpq ${'$'}0, 48(%rsp)
	jne L4
L3:
	movq ${'$'}0, 56(%rsp)
;mem (56(%rsp)) -> mem (24(%rsp)) move through temporary register
	movq 56(%rsp), %r10
	movq %r10, 24(%rsp)
	jmp L5
L4:
	movq ${'$'}1, 72(%rsp)
;mem (72(%rsp)) -> mem (24(%rsp)) move through temporary register
	movq 72(%rsp), %r10
	movq %r10, 24(%rsp)
L5:
;mem (24(%rsp)) -> mem (88(%rsp)) move through temporary register
	movq 24(%rsp), %r10
	movq %r10, 88(%rsp)
;mem (88(%rsp)) -> mem (16(%rsp)) move through temporary register
	movq 88(%rsp), %r10
	movq %r10, 16(%rsp)
L2:
;mem (16(%rsp)) -> mem (104(%rsp)) move through temporary register
	movq 16(%rsp), %r10
	movq %r10, 104(%rsp)
	movq 104(%rsp), %rax
	addq ${'$'}104, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "(if #t 1 (if #f 2 3))"
        ))
    }

    @Test
    fun `test while`() {
        testCodegen("""
main.S:
print:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}8, %rsp
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), %rax
	addq ${'$'}8, %rsp
	popq %rbp
	retq

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}40, %rsp
L2:
	movq print, 8(%rsp)
	movq ${'$'}0, 16(%rsp)
	cmpq ${'$'}0, 16(%rsp)
	jne L1
L0:
	movq ${'$'}0, 24(%rsp)
;save registers for call print
	movq 24(%rsp), %rdi
	callq print
	movq %rax, 32(%rsp)
;restore registers for call print
;finish handling call print
	jmp L2
L1:
	movq ${'$'}0, 40(%rsp)
	movq 40(%rsp), %rax
	addq ${'$'}40, %rsp
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
	subq ${'$'}40, %rsp
	movq ${'$'}0, 16(%rsp)
;mem (16(%rsp)) -> mem (8(%rsp)) move through temporary register
	movq 16(%rsp), %r10
	movq %r10, 8(%rsp)
	movq ${'$'}0, 32(%rsp)
;mem (32(%rsp)) -> mem (8(%rsp)) move through temporary register
	movq 32(%rsp), %r10
	movq %r10, 8(%rsp)
	movq 40(%rsp), %rax
	addq ${'$'}40, %rsp
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
	subq ${'$'}32, %rsp
	movq ${'$'}0, 16(%rsp)
;mem (16(%rsp)) -> mem (8(%rsp)) move through temporary register
	movq 16(%rsp), %r10
	movq %r10, 8(%rsp)
;mem (8(%rsp)) -> mem (32(%rsp)) move through temporary register
	movq 8(%rsp), %r10
	movq %r10, 32(%rsp)
	movq 32(%rsp), %rax
	addq ${'$'}32, %rsp
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
.asciz "let"
.asciz "foo"
.asciz "x"
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}128, %rsp
	movq ${'$'}0, 8(%rsp)
	movabsq Lstr0, 112(%rsp)
;save registers for call r__createSymbol
	movq 112(%rsp), %rdi
	callq r__createSymbol
	movq %rax, 16(%rsp)
;restore registers for call r__createSymbol
;finish handling call r__createSymbol
;save registers for call r__withElement
	movq 16(%rsp), %rdi
	movq 8(%rsp), %rsi
	callq r__withElement
	movq %rax, 24(%rsp)
;restore registers for call r__withElement
;finish handling call r__withElement
	movq ${'$'}0, 32(%rsp)
	movabsq Lstr1, 120(%rsp)
;save registers for call r__createString
	movq 120(%rsp), %rdi
	callq r__createString
	movq %rax, 40(%rsp)
;restore registers for call r__createString
;finish handling call r__createString
;save registers for call r__withElement
	movq 40(%rsp), %rdi
	movq 32(%rsp), %rsi
	callq r__withElement
	movq %rax, 48(%rsp)
;restore registers for call r__withElement
;finish handling call r__withElement
	movq ${'$'}0, 56(%rsp)
;save registers for call r__withElement
	movq 56(%rsp), %rdi
	movq 48(%rsp), %rsi
	callq r__withElement
	movq %rax, 64(%rsp)
;restore registers for call r__withElement
;finish handling call r__withElement
	movq ${'$'}0, 72(%rsp)
;save registers for call r__withElement
	movq 72(%rsp), %rdi
	movq 64(%rsp), %rsi
	callq r__withElement
	movq %rax, 80(%rsp)
;restore registers for call r__withElement
;finish handling call r__withElement
;save registers for call r__withElement
	movq 80(%rsp), %rdi
	movq 24(%rsp), %rsi
	callq r__withElement
	movq %rax, 88(%rsp)
;restore registers for call r__withElement
;finish handling call r__withElement
	movabsq Lstr2, 128(%rsp)
;save registers for call r__createSymbol
	movq 128(%rsp), %rdi
	callq r__createSymbol
	movq %rax, 96(%rsp)
;restore registers for call r__createSymbol
;finish handling call r__createSymbol
;save registers for call r__withElement
	movq 96(%rsp), %rdi
	movq 88(%rsp), %rsi
	callq r__withElement
	movq %rax, 104(%rsp)
;restore registers for call r__withElement
;finish handling call r__withElement
	movq 104(%rsp), %rax
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
	subq ${'$'}8, %rsp
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), %rax
	addq ${'$'}8, %rsp
	popq %rbp
	retq

main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}64, %rsp
	movq foo, 8(%rsp)
	movq ${'$'}1, 16(%rsp)
	movq ${'$'}0, 24(%rsp)
	movq ${'$'}0, 32(%rsp)
;save registers for call r__withElement
	movq 32(%rsp), %rdi
	movq 24(%rsp), %rsi
	callq r__withElement
	movq %rax, 40(%rsp)
;restore registers for call r__withElement
;finish handling call r__withElement
	movq ${'$'}1, 48(%rsp)
;save registers for call r__withElement
	movq 48(%rsp), %rdi
	movq 40(%rsp), %rsi
	callq r__withElement
	movq %rax, 56(%rsp)
;restore registers for call r__withElement
;finish handling call r__withElement
;save registers for call foo
	movq 16(%rsp), %rdi
	movq 56(%rsp), %rsi
	callq foo
	movq %rax, 64(%rsp)
;restore registers for call foo
;finish handling call foo
	movq 64(%rsp), %rax
	addq ${'$'}64, %rsp
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

    fun testCodegen(expected: String, files: List<InMemoryFileInfo>) {
        val sources = files.map { InMemorySource(it.text, it.name) }
        val session = frontend.compilationSession(sources, emptyList(), CompilerConfig(0))
        val lir = session.getLir().unwrap()

        val backend = AssemblyBackend(TextAssembler(), NaiveRegisterAllocator())
        val actual = lir.joinToString("\n") {
            val artifactBuilder = StringArtifactBuilder()
            backend.runBackend(BackendConfiguration(""), it, artifactBuilder)
            artifactBuilder.sb.toString()
        }
        assertEquals(expected.trim(), actual.trim())
    }
}