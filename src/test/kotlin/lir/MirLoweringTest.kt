package lir

import FrontendTest
import InMemoryFileInfo
import util.InMemorySource
import frontend.CompilerConfig
import withText
import kotlin.test.Test
import kotlin.test.assertEquals

class LirLoweringTest : FrontendTest(emptyList()) {
    @Test
    fun `test top level list`() {
        testMir("""
fun main__init :  virtual regs: 0
  0 inplace_i64 reg: %0 value: 0
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "()"
        ))
    }

    @Test
    fun `test function definition`() {
        testMir("""
fun foo :  virtual regs: 1
  0 inplace_i64 reg: %0 value: 0
  1 return %0

fun main__init :  virtual regs: 0
  0 get_function_ptr foo %0
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "(defn foo(x) 42)"
        ))
    }

    @Test
    fun `test if`() {
        testMir("""
fun main__init :  virtual regs: 1
  0 inplace_i64 reg: %0 value: 0
  1 cond_jump cond: %0 thenIndex: 2 elseIndex: 5
  2 inplace_i64 reg: %1 value: 1
  3 mov from %1 to %0
  4 goto 7
  5 inplace_i64 reg: %3 value: 0
  6 mov from %3 to %0
  7 mov from %0 to %5
  8 return %5
        """.trimIndent(), listOf(
                "main" withText "(if #t 1 2)"
        ))
    }

    @Test
    fun `test if nested`() {
        testMir("""
fun main__init :  virtual regs: 2
  0 inplace_i64 reg: %0 value: 0
  1 cond_jump cond: %0 thenIndex: 2 elseIndex: 5
  2 inplace_i64 reg: %1 value: 1
  3 mov from %1 to %0
  4 goto 14
  5 inplace_i64 reg: %3 value: 0
  6 cond_jump cond: %3 thenIndex: 7 elseIndex: 10
  7 inplace_i64 reg: %4 value: 0
  8 mov from %4 to %0
  9 goto 12
  10 inplace_i64 reg: %6 value: 1
  11 mov from %6 to %0
  12 mov from %0 to %8
  13 mov from %8 to %0
  14 mov from %0 to %10
  15 return %10
        """.trimIndent(), listOf(
                "main" withText "(if #t 1 (if #f 2 3))"
        ))
    }

    @Test
    fun `test while`() {
        testMir("""
fun print :  virtual regs: 1
  0 inplace_i64 reg: %0 value: 0
  1 return %0

fun main__init :  virtual regs: 0
  0 get_function_ptr print %0
  1 inplace_i64 reg: %1 value: 0
  2 cond_jump cond: %1 thenIndex: 3 elseIndex: 6
  3 inplace_i64 reg: %2 value: 0
  4 call args: print
  5 goto 0
  6 inplace_i64 reg: %4 value: 0
  7 return %4
        """.trimIndent(), listOf(
                "main" withText "(defn print (x) ())(while #t (print 42))"
        ))
    }

    @Test
    fun `test assign`() {
        testMir("""
fun foo :  virtual regs: 1
  0 inplace_i64 reg: %0 value: 0
  1 mov from %0 to %0
  2 return %1

fun main__init :  virtual regs: 0
  0 get_function_ptr foo %0
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "(defn foo (x) (set x 12))"
        ))
    }

    @Test
    fun `test let`() {
        testMir("""
fun main__init :  virtual regs: 1
  0 inplace_i64 reg: %0 value: 0
  1 mov from %0 to %0
  2 mov from %0 to %2
  3 return %2
        """.trimIndent(), listOf(
                "main" withText "(let ((x 12)) x)"
        ))
    }

    @Test
    fun `test data`() {
        testMir("""
fun main__init :  virtual regs: 3
  0 inplace_i64 reg: %0 value: 0
  1 get_str_ptr strIndex: 0
  2 call args: r__createSymbol
  3 call args: r__withElement
  4 inplace_i64 reg: %3 value: 0
  5 get_str_ptr strIndex: 1
  6 call args: r__createString
  7 call args: r__withElement
  8 inplace_i64 reg: %6 value: 0
  9 call args: r__withElement
  10 inplace_i64 reg: %8 value: 0
  11 call args: r__withElement
  12 call args: r__withElement
  13 get_str_ptr strIndex: 2
  14 call args: r__createSymbol
  15 call args: r__withElement
  16 return %12
        """.trimIndent(), listOf(
                "main" withText "`(let (\"foo\" 12 2) x)"
        ))
    }


    @Test
    fun `test vararg`() {
        testMir("""
fun foo :  virtual regs: 2
  0 inplace_i64 reg: %0 value: 0
  1 return %0

fun main__init :  virtual regs: 0
  0 get_function_ptr foo %0
  1 inplace_i64 reg: %1 value: 1
  2 inplace_i64 reg: %2 value: 0
  3 inplace_i64 reg: %3 value: 0
  4 call args: r__withElement
  5 inplace_i64 reg: %5 value: 1
  6 call args: r__withElement
  7 call args: foo
  8 return %7
        """.trimIndent(), listOf(
                "main" withText "(defn foo (a @vp) ())(foo 1 2 3)"
        ))
    }


    @Test
    fun `test call by variable`() {
        testMir("""
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

    fun testMir(expected: String, files: List<InMemoryFileInfo>) {
        val sources = files.map { InMemorySource(it.text, it.name) }
        val session = frontend.compilationSession(sources, emptyList(), CompilerConfig(0))
        val lir = session.getLir().unwrap()
        val actual = lir.joinToString("\n") { it.toString() }
        assertEquals(expected, actual)
    }
}