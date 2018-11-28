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
	subq ${'$'}8, %rsp
	movq ${'$'}0, 8(%rsp)
	cmpq ${'$'}0, 8(%rsp)
	jne L1
L0:
	movq ${'$'}1, 8(%rsp)
	movq 8(%rsp), 8(%rsp)
	jmp L2
L1:
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), 8(%rsp)
L2:
	movq 8(%rsp), 8(%rsp)
	movq 8(%rsp), %rax
	addq ${'$'}8, %rsp
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
	subq ${'$'}8, %rsp
	movq ${'$'}0, 8(%rsp)
	cmpq ${'$'}0, 8(%rsp)
	jne L1
L0:
	movq ${'$'}1, 8(%rsp)
	movq 8(%rsp), 8(%rsp)
	jmp L2
L1:
	movq ${'$'}0, 8(%rsp)
	cmpq ${'$'}0, 8(%rsp)
	jne L4
L3:
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), 8(%rsp)
	jmp L5
L4:
	movq ${'$'}1, 8(%rsp)
	movq 8(%rsp), 8(%rsp)
L5:
	movq 8(%rsp), 8(%rsp)
	movq 8(%rsp), 8(%rsp)
L2:
	movq 8(%rsp), 8(%rsp)
	movq 8(%rsp), %rax
	addq ${'$'}8, %rsp
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
	subq ${'$'}8, %rsp
L2:
	movq print, 8(%rsp)
	movq ${'$'}0, 8(%rsp)
	cmpq ${'$'}0, 8(%rsp)
	jne L1
L0:
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), %rdi
	callq print
	movq %rax, 8(%rsp)
	jmp L2
L1:
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), %rax
	addq ${'$'}8, %rsp
	popq %rbp
	retq
        """.trimIndent(), listOf(
                "main" withText "(defn print (x) ())(while #t (print 42))"
        ))
    }

    @Test
    fun `test assign`() {
        testCodegen("""
fun foo :  virtual regs: 1 paramCount: 1
  0 inplace_i64 reg: %0 value: 0
  1 mov from %0 to %0
  2 return %1

fun main__init :  virtual regs: 0 paramCount: 0
  0 get_function_ptr foo %0
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "(defn foo (x) (set x 12))"
        ))
    }

    @Test
    fun `test let`() {
        testCodegen("""
main.S:
main__init:
	pushq %rbp
	movq %rsp, %rbp
	subq ${'$'}8, %rsp
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), 8(%rsp)
	movq 8(%rsp), 8(%rsp)
	movq 8(%rsp), %rax
	addq ${'$'}8, %rsp
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
	subq ${'$'}8, %rsp
	movq ${'$'}0, 8(%rsp)
	movabsq Lstr0, 8(%rsp)
	movq 8(%rsp), %rdi
	callq r__createSymbol
	movq %rax, 8(%rsp)
	movq 8(%rsp), %rdi
	movq 8(%rsp), %rsi
	callq r__withElement
	movq %rax, 8(%rsp)
	movq ${'$'}0, 8(%rsp)
	movabsq Lstr1, 8(%rsp)
	movq 8(%rsp), %rdi
	callq r__createString
	movq %rax, 8(%rsp)
	movq 8(%rsp), %rdi
	movq 8(%rsp), %rsi
	callq r__withElement
	movq %rax, 8(%rsp)
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), %rdi
	movq 8(%rsp), %rsi
	callq r__withElement
	movq %rax, 8(%rsp)
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), %rdi
	movq 8(%rsp), %rsi
	callq r__withElement
	movq %rax, 8(%rsp)
	movq 8(%rsp), %rdi
	movq 8(%rsp), %rsi
	callq r__withElement
	movq %rax, 8(%rsp)
	movabsq Lstr2, 8(%rsp)
	movq 8(%rsp), %rdi
	callq r__createSymbol
	movq %rax, 8(%rsp)
	movq 8(%rsp), %rdi
	movq 8(%rsp), %rsi
	callq r__withElement
	movq %rax, 8(%rsp)
	movq 8(%rsp), %rax
	addq ${'$'}8, %rsp
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
	subq ${'$'}8, %rsp
	movq foo, 8(%rsp)
	movq ${'$'}1, 8(%rsp)
	movq ${'$'}0, 8(%rsp)
	movq ${'$'}0, 8(%rsp)
	movq 8(%rsp), %rdi
	movq 8(%rsp), %rsi
	callq r__withElement
	movq %rax, 8(%rsp)
	movq ${'$'}1, 8(%rsp)
	movq 8(%rsp), %rdi
	movq 8(%rsp), %rsi
	callq r__withElement
	movq %rax, 8(%rsp)
	movq 8(%rsp), %rdi
	movq 8(%rsp), %rsi
	callq foo
	movq %rax, 8(%rsp)
	movq 8(%rsp), %rax
	addq ${'$'}8, %rsp
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