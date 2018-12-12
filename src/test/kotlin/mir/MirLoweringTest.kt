package mir

import FrontendTest
import InMemoryFileInfo
import util.InMemorySource
import frontend.CompilerConfig
import withText
import kotlin.test.Test
import kotlin.test.assertEquals

class MirLoweringTest : FrontendTest(emptyList()) {
    @Test
    fun `test top level list`() {
        testMir("""
main:
fun main__init params: 0, totalVars: 0 (main)
b0:
  load_const ()
  return b0:i0
        """.trimIndent(), listOf(
                "main" withText "()"
        ))
    }

    @Test
    fun `test function definition`() {
        testMir("""
main:
fun foo params: 1, totalVars: 1
var table:
   0 x
b0:
  load_const 42 (i32, tagged)
  return b0:i0

fun main__init params: 0, totalVars: 0 (main)
b0:
  get_function_reference 0
  return b0:i0
        """.trimIndent(), listOf(
                "main" withText "(defn foo(x) 42)"
        ))
    }

    @Test
    fun `test if`() {
        testMir("""
main:
fun main__init params: 0, totalVars: 1 (main)
var table:
   0 __merge_if_0
b0:
  load_const true (bool, tagged)
  cond_jump cond: b0:i0 then: b1 else: b2
b1:
  load_const 1 (i32, tagged)
  store_var: v1 value: b1:i0
  goto b3
b2:
  load_const 2 (i32, tagged)
  store_var: v1 value: b2:i0
  goto b3
b3:
  load_var: v1
  return b3:i0
        """.trimIndent(), listOf(
                "main" withText "(if #t 1 2)"
        ))
    }

    @Test
    fun `test if nested`() {
        testMir("""
main:
fun main__init params: 0, totalVars: 2 (main)
var table:
   0 __merge_if_0
   1 __merge_if_1
b0:
  load_const true (bool, tagged)
  cond_jump cond: b0:i0 then: b1 else: b2
b1:
  load_const 1 (i32, tagged)
  store_var: v1 value: b1:i0
  goto b6
b2:
  load_const false (bool, tagged)
  cond_jump cond: b2:i0 then: b3 else: b4
b3:
  load_const 2 (i32, tagged)
  store_var: v2 value: b3:i0
  goto b5
b4:
  load_const 3 (i32, tagged)
  store_var: v2 value: b4:i0
  goto b5
b5:
  load_var: v2
  store_var: v1 value: b5:i0
  goto b6
b6:
  load_var: v1
  return b6:i0
        """.trimIndent(), listOf(
                "main" withText "(if #t 1 (if #f 2 3))"
        ))
    }

    @Test
    fun `test while`() {
        testMir("""
main:
fun print params: 1, totalVars: 1
var table:
   0 x
b0:
  load_const ()
  return b0:i0

fun main__init params: 0, totalVars: 0 (main)
b0:
  get_function_reference 0
  load_const true (bool, tagged)
  cond_jump cond: b0:i1 then: b1 else: b2
b1:
  load_const 42 (i32, tagged)
  call function: 0 args: (b1:i0)
  goto b0
b2:
  load_const ()
  return b2:i0
        """.trimIndent(), listOf(
                "main" withText "(defn print (x) ())(while #t (print 42))"
        ))
    }

    @Test
    fun `test assign`() {
        testMir("""
main:
fun main__init params: 0, totalVars: 1 (main)
var table:
   0 y
b0:
  load_const 0 (i32, tagged)
  store_var: v0 value: b0:i0
  load_const 12 (i32, tagged)
  store_var: v0 value: b0:i2
  return b0:i3
        """.trimIndent(), listOf(
                "main" withText "(let (( y 0)) (set y 12))"
        ))
    }

    @Test
    fun `test let`() {
        testMir("""
main:
fun main__init params: 0, totalVars: 1 (main)
var table:
   0 x
b0:
  load_const 12 (i32, tagged)
  store_var: v0 value: b0:i0
  load_var: v0
  return b0:i2
        """.trimIndent(), listOf(
                "main" withText "(let ((x 12)) x)"
        ))
    }

    @Test
    fun `test data`() {
        testMir("""
main:
fun main__init params: 0, totalVars: 0 (main)
b0:
  load_const ()
  load_const let (symbol)
  with_element list: b0:i1, value: b0:i1
  load_const ()
  load_const foo (string, tagged)
  with_element list: b0:i4, value: b0:i4
  load_const 12 (i32, tagged)
  with_element list: b0:i6, value: b0:i6
  load_const 2 (i32, tagged)
  with_element list: b0:i8, value: b0:i8
  with_element list: b0:i9, value: b0:i9
  load_const x (symbol)
  with_element list: b0:i11, value: b0:i11
  return b0:i12
        """.trimIndent(), listOf(
                "main" withText "`(let (\"foo\" 12 2) x)"
        ))
    }


    @Test
    fun `test vararg`() {
        testMir("""
main:
fun foo params: 2, totalVars: 2
var table:
   0 a
   1 vp
b0:
  load_const ()
  return b0:i0

fun main__init params: 0, totalVars: 0 (main)
b0:
  get_function_reference 0
  load_const 1 (i32, tagged)
  load_const ()
  load_const 2 (i32, tagged)
  with_element list: b0:i3, value: b0:i3
  load_const 3 (i32, tagged)
  with_element list: b0:i5, value: b0:i5
  call function: 0 args: (b0:i1, b0:i6)
  return b0:i7
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
var table:
   0 f
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
    fun `test print list with value`() {
        testMir("""
fun main__init :  virtual regs: 5 paramCount: 0
  0 get_function_ptr r__print %0
  1 inplace_i64 reg: %1 value: 0 (without tag: 0)
  2 inplace_i64 reg: %2 value: 2305843009213693964 (without tag: 12)
  3 call name: r__withElement resultReg: %3 args: (%1, %2)
  4 call name: r__print resultReg: %4 args: (%3)
  5 return %4
        """.trimIndent(), listOf(
                "main" withText "(defnat print r__print (x))(print `(12 22))"
        ))
    }

    fun testMir(expected: String, files: List<InMemoryFileInfo>) {
        val sources = files.map { InMemorySource(it.text, it.name) }
        val session = frontend.compilationSession(sources, emptyList(), CompilerConfig(0), false)
        val mir = session.getMir().unwrap()
        val actual = mir.joinToString("\n") { it.toString() }
        assertEquals(expected, actual)
    }
}