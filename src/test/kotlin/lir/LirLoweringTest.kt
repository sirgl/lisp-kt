package lir

import FrontendTest
import InMemoryFileInfo
import frontend.CompilerConfig
import util.InMemorySource
import withText
import kotlin.test.Test
import kotlin.test.assertEquals

class LirLoweringTest : FrontendTest(emptyList()) {
    @Test
    fun `test top level list`() {
        testLir("""
fun main__init :  virtual regs: 1 paramCount: 0
  0 inplace_i64 reg: %0 value: 0 (without tag: 0)
  1 return %0

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "()"
        ))
    }

    @Test
    fun `test function definition`() {
        testLir("""
String table:
   0 Unexpected parameter count
fun foo :  virtual regs: 2 paramCount: 1
  0 inplace_i64 reg: %1 value: 2305843009213693994 (without tag: 42)
  1 return %1

fun foo_satellite :  virtual regs: 11 paramCount: 1
  0 mov from %0 to %0
  1 call name: r__size resultReg: %1 args: (%0)
  2 inplace_i64 reg: %2 value: 2305843009213693953 (without tag: 1)
  3 call name: r__eq resultReg: %3 args: (%1, %2)
  4 call name: r__untag resultReg: %4 args: (%3)
  5 cond_jump cond: %4 thenIndex: 6 elseIndex: 10
  6 call name: r__first resultReg: %5 args: (%0)
  7 call name: r__tail resultReg: %6 args: (%0)
  8 call name: foo resultReg: %7 args: (%5)
  9 return %7
  10 get_str_ptr strIndex: 0 dst: %10
  11 call name: r__createString resultReg: %8 args: (%10)
  12 call name: r__printErrorAndExit resultReg: %9 args: (%8)

fun main__init :  virtual regs: 2 paramCount: 0
  0 get_function_ptr foo_satellite %1
  1 call name: r__tagFunction resultReg: %0 args: (%1)
  2 return %0

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "(defn foo(x) 42)"
        ))
    }

    @Test
    fun `test if`() {
        testLir("""
fun main__init :  virtual regs: 8 paramCount: 0
  0 inplace_i64 reg: %1 value: 4611686018427387905 (without tag: 1)
  1 call name: r__untag resultReg: %2 args: (%1)
  2 cond_jump cond: %2 thenIndex: 3 elseIndex: 6
  3 inplace_i64 reg: %3 value: 2305843009213693953 (without tag: 1)
  4 mov from %3 to %1
  5 goto 8
  6 inplace_i64 reg: %5 value: 2305843009213693954 (without tag: 2)
  7 mov from %5 to %1
  8 mov from %1 to %7
  9 return %7

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "(if #t 1 2)"
        ))
    }

    @Test
    fun `test if nested`() {
        testLir("""
fun main__init :  virtual regs: 15 paramCount: 0
  0 inplace_i64 reg: %2 value: 4611686018427387905 (without tag: 1)
  1 call name: r__untag resultReg: %3 args: (%2)
  2 cond_jump cond: %3 thenIndex: 3 elseIndex: 6
  3 inplace_i64 reg: %4 value: 2305843009213693953 (without tag: 1)
  4 mov from %4 to %1
  5 goto 16
  6 inplace_i64 reg: %6 value: 4611686018427387904 (without tag: 0)
  7 call name: r__untag resultReg: %7 args: (%6)
  8 cond_jump cond: %7 thenIndex: 9 elseIndex: 12
  9 inplace_i64 reg: %8 value: 2305843009213693954 (without tag: 2)
  10 mov from %8 to %2
  11 goto 14
  12 inplace_i64 reg: %10 value: 2305843009213693955 (without tag: 3)
  13 mov from %10 to %2
  14 mov from %2 to %12
  15 mov from %12 to %1
  16 mov from %1 to %14
  17 return %14

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "(if #t 1 (if #f 2 3))"
        ))
    }

    @Test
    fun `test while`() {
        testLir("""
String table:
   0 Unexpected parameter count
fun print :  virtual regs: 2 paramCount: 1
  0 inplace_i64 reg: %1 value: 0 (without tag: 0)
  1 return %1

fun print_satellite :  virtual regs: 11 paramCount: 1
  0 mov from %0 to %0
  1 call name: r__size resultReg: %1 args: (%0)
  2 inplace_i64 reg: %2 value: 2305843009213693953 (without tag: 1)
  3 call name: r__eq resultReg: %3 args: (%1, %2)
  4 call name: r__untag resultReg: %4 args: (%3)
  5 cond_jump cond: %4 thenIndex: 6 elseIndex: 10
  6 call name: r__first resultReg: %5 args: (%0)
  7 call name: r__tail resultReg: %6 args: (%0)
  8 call name: print resultReg: %7 args: (%5)
  9 return %7
  10 get_str_ptr strIndex: 0 dst: %10
  11 call name: r__createString resultReg: %8 args: (%10)
  12 call name: r__printErrorAndExit resultReg: %9 args: (%8)

fun main__init :  virtual regs: 7 paramCount: 0
  0 get_function_ptr print_satellite %6
  1 call name: r__tagFunction resultReg: %0 args: (%6)
  2 inplace_i64 reg: %1 value: 4611686018427387905 (without tag: 1)
  3 call name: r__untag resultReg: %2 args: (%1)
  4 cond_jump cond: %2 thenIndex: 5 elseIndex: 8
  5 inplace_i64 reg: %3 value: 2305843009213693994 (without tag: 42)
  6 call name: print resultReg: %4 args: (%3)
  7 goto 2
  8 inplace_i64 reg: %5 value: 0 (without tag: 0)
  9 return %5

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "(defn print (x) ())(while #t (print 42))"
        ))
    }

    @Test
    fun `test assign`() {
        testLir("""
fun main__init :  virtual regs: 5 paramCount: 0
  0 inplace_i64 reg: %1 value: 2305843009213693952 (without tag: 0)
  1 mov from %1 to %0
  2 inplace_i64 reg: %3 value: 2305843009213693964 (without tag: 12)
  3 mov from %3 to %0
  4 return %4

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "(let (( y 0)) (set y 12))"
        ))
    }

    @Test
    fun `test let`() {
        testLir("""
fun main__init :  virtual regs: 4 paramCount: 0
  0 inplace_i64 reg: %1 value: 2305843009213693964 (without tag: 12)
  1 mov from %1 to %0
  2 mov from %0 to %3
  3 return %3

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "(let ((x 12)) x)"
        ))
    }

    @Test
    fun `test data`() {
        testLir("""
String table:
   0 x
   1 foo
   2 let
fun main__init :  virtual regs: 16 paramCount: 0
  0 inplace_i64 reg: %0 value: 0 (without tag: 0)
  1 get_str_ptr strIndex: 0 dst: %13
  2 call name: r__createSymbol resultReg: %1 args: (%13)
  3 call name: r__withElement resultReg: %2 args: (%0, %1)
  4 inplace_i64 reg: %3 value: 0 (without tag: 0)
  5 inplace_i64 reg: %4 value: 2305843009213693954 (without tag: 2)
  6 call name: r__withElement resultReg: %5 args: (%3, %4)
  7 inplace_i64 reg: %6 value: 2305843009213693964 (without tag: 12)
  8 call name: r__withElement resultReg: %7 args: (%5, %6)
  9 get_str_ptr strIndex: 1 dst: %14
  10 call name: r__createString resultReg: %8 args: (%14)
  11 call name: r__withElement resultReg: %9 args: (%7, %8)
  12 call name: r__withElement resultReg: %10 args: (%2, %9)
  13 get_str_ptr strIndex: 2 dst: %15
  14 call name: r__createSymbol resultReg: %11 args: (%15)
  15 call name: r__withElement resultReg: %12 args: (%10, %11)
  16 return %12

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "`(let (\"foo\" 12 2) x)"
        ))
    }


    @Test
    fun `test vararg`() {
        testLir("""
String table:
   0 Unexpected parameter count
fun foo :  virtual regs: 3 paramCount: 2
  0 inplace_i64 reg: %2 value: 0 (without tag: 0)
  1 return %2

fun foo_satellite :  virtual regs: 11 paramCount: 1
  0 mov from %0 to %0
  1 call name: r__size resultReg: %1 args: (%0)
  2 inplace_i64 reg: %2 value: 2305843009213693954 (without tag: 2)
  3 call name: _ge resultReg: %3 args: (%1, %2)
  4 call name: r__untag resultReg: %4 args: (%3)
  5 cond_jump cond: %4 thenIndex: 6 elseIndex: 10
  6 call name: r__first resultReg: %5 args: (%0)
  7 call name: r__tail resultReg: %6 args: (%0)
  8 call name: foo resultReg: %7 args: (%5, %6)
  9 return %7
  10 get_str_ptr strIndex: 0 dst: %10
  11 call name: r__createString resultReg: %8 args: (%10)
  12 call name: r__printErrorAndExit resultReg: %9 args: (%8)

fun main__init :  virtual regs: 9 paramCount: 0
  0 get_function_ptr foo_satellite %8
  1 call name: r__tagFunction resultReg: %0 args: (%8)
  2 inplace_i64 reg: %1 value: 2305843009213693953 (without tag: 1)
  3 inplace_i64 reg: %2 value: 0 (without tag: 0)
  4 inplace_i64 reg: %3 value: 2305843009213693954 (without tag: 2)
  5 call name: r__withElement resultReg: %4 args: (%2, %3)
  6 inplace_i64 reg: %5 value: 2305843009213693955 (without tag: 3)
  7 call name: r__withElement resultReg: %6 args: (%4, %5)
  8 call name: foo resultReg: %7 args: (%1, %6)
  9 return %7

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "(defn foo (a @vp) ())(foo 1 2 3)"
        ))
    }


    @Test
    fun `test call by variable`() {
        testLir("""
String table:
   0 Unexpected parameter count
fun f :  virtual regs: 1 paramCount: 0
  0 inplace_i64 reg: %0 value: 2305843009213693994 (without tag: 42)
  1 return %0

fun f_satellite :  virtual regs: 9 paramCount: 1
  0 mov from %0 to %0
  1 call name: r__size resultReg: %1 args: (%0)
  2 inplace_i64 reg: %2 value: 2305843009213693952 (without tag: 0)
  3 call name: r__eq resultReg: %3 args: (%1, %2)
  4 call name: r__untag resultReg: %4 args: (%3)
  5 cond_jump cond: %4 thenIndex: 6 elseIndex: 8
  6 call name: f resultReg: %5 args: ()
  7 return %5
  8 get_str_ptr strIndex: 0 dst: %8
  9 call name: r__createString resultReg: %6 args: (%8)
  10 call name: r__printErrorAndExit resultReg: %7 args: (%6)

fun main__init :  virtual regs: 8 paramCount: 0
  0 get_function_ptr f_satellite %7
  1 call name: r__tagFunction resultReg: %1 args: (%7)
  2 mov from %1 to %0
  3 mov from %0 to %3
  4 inplace_i64 reg: %4 value: 0 (without tag: 0)
  5 call name: r__untag resultReg: %5 args: (%3)
  6 call_by_ptr: function: 5 args: 4 result: 6
  7 return %6

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
            "main" withText """
            (let ((x (defn f () 42) )) (x))
                """.trimIndent()
        ))
    }

    @Test
    fun `test print list with value`() {
        testLir("""
String table:
   0 Unexpected parameter count
fun print_satellite :  virtual regs: 11 paramCount: 1
  0 mov from %0 to %0
  1 call name: r__size resultReg: %1 args: (%0)
  2 inplace_i64 reg: %2 value: 2305843009213693953 (without tag: 1)
  3 call name: r__eq resultReg: %3 args: (%1, %2)
  4 call name: r__untag resultReg: %4 args: (%3)
  5 cond_jump cond: %4 thenIndex: 6 elseIndex: 10
  6 call name: r__first resultReg: %5 args: (%0)
  7 call name: r__tail resultReg: %6 args: (%0)
  8 call name: r__print resultReg: %7 args: (%5)
  9 return %7
  10 get_str_ptr strIndex: 0 dst: %10
  11 call name: r__createString resultReg: %8 args: (%10)
  12 call name: r__printErrorAndExit resultReg: %9 args: (%8)

fun main__init :  virtual regs: 6 paramCount: 0
  0 get_function_ptr print_satellite %5
  1 call name: r__tagFunction resultReg: %0 args: (%5)
  2 inplace_i64 reg: %1 value: 0 (without tag: 0)
  3 inplace_i64 reg: %2 value: 2305843009213693964 (without tag: 12)
  4 call name: r__withElement resultReg: %3 args: (%1, %2)
  5 call name: r__print resultReg: %4 args: (%3)
  6 return %4

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
                "main" withText "(defnat print r__print (x))(print `(12))"
        ))
    }

    @Test
    fun `test simple call`() {
        testLir("""
String table:
   0 Unexpected parameter count
   1 Hello
fun print_satellite :  virtual regs: 11 paramCount: 1
  0 mov from %0 to %0
  1 call name: r__size resultReg: %1 args: (%0)
  2 inplace_i64 reg: %2 value: 2305843009213693953 (without tag: 1)
  3 call name: r__eq resultReg: %3 args: (%1, %2)
  4 call name: r__untag resultReg: %4 args: (%3)
  5 cond_jump cond: %4 thenIndex: 6 elseIndex: 10
  6 call name: r__first resultReg: %5 args: (%0)
  7 call name: r__tail resultReg: %6 args: (%0)
  8 call name: r__print resultReg: %7 args: (%5)
  9 return %7
  10 get_str_ptr strIndex: 0 dst: %10
  11 call name: r__createString resultReg: %8 args: (%10)
  12 call name: r__printErrorAndExit resultReg: %9 args: (%8)

fun main__init :  virtual regs: 5 paramCount: 0
  0 get_function_ptr print_satellite %3
  1 call name: r__tagFunction resultReg: %0 args: (%3)
  2 get_str_ptr strIndex: 1 dst: %4
  3 call name: r__createString resultReg: %1 args: (%4)
  4 call name: r__print resultReg: %2 args: (%1)
  5 return %2

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """.trimIndent(), listOf(
                "main" withText """
        (defnat print r__print (x))
        (print "Hello")
                """.trimIndent()
        ))
    }


    @Test
    fun `test string literal`() {
        testLir("""
String table:
   0 Hello world!
fun main__init :  virtual regs: 2 paramCount: 0
  0 get_str_ptr strIndex: 0 dst: %1
  1 call name: r__createString resultReg: %0 args: (%1)
  2 return %0

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """, listOf(
                "main" withText """
        "Hello world!"
                """.trimIndent()
        ))
    }

    @Test
    fun `test string literal inside print`() {
        testLir("""
String table:
   0 Unexpected parameter count
   1 Hello world!
fun print_satellite :  virtual regs: 11 paramCount: 1
  0 mov from %0 to %0
  1 call name: r__size resultReg: %1 args: (%0)
  2 inplace_i64 reg: %2 value: 2305843009213693953 (without tag: 1)
  3 call name: r__eq resultReg: %3 args: (%1, %2)
  4 call name: r__untag resultReg: %4 args: (%3)
  5 cond_jump cond: %4 thenIndex: 6 elseIndex: 10
  6 call name: r__first resultReg: %5 args: (%0)
  7 call name: r__tail resultReg: %6 args: (%0)
  8 call name: print resultReg: %7 args: (%5)
  9 return %7
  10 get_str_ptr strIndex: 0 dst: %10
  11 call name: r__createString resultReg: %8 args: (%10)
  12 call name: r__printErrorAndExit resultReg: %9 args: (%8)

fun main__init :  virtual regs: 5 paramCount: 0
  0 get_function_ptr print_satellite %3
  1 call name: r__tagFunction resultReg: %0 args: (%3)
  2 get_str_ptr strIndex: 1 dst: %4
  3 call name: r__createString resultReg: %1 args: (%4)
  4 call name: print resultReg: %2 args: (%1)
  5 return %2

fun __entry__ :  virtual regs: 1 paramCount: 0
  0 call name: main__init resultReg: %0 args: ()
  1 return %0
        """, listOf(
                "main" withText """
        (defnat print print (x))
        (print "Hello world!")
                """.trimIndent()
        ))
    }


    fun testLir(expected: String, files: List<InMemoryFileInfo>) {
        val sources = files.map { InMemorySource(it.text, it.name) }
        val session = frontend.compilationSession(sources, emptyList(), CompilerConfig(0), false)
        val lir = session.getLir().unwrap()
        val actual = lir.joinToString("\n") { it.toString() }
//        println(lir.first().functions.first().toBBGraph())
        assertEquals(expected.trim(), actual.trim())
    }
}