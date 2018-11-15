package interpreter

import lexer.Lexer
import lexer.LexerImpl
import org.junit.jupiter.api.Test
import parser.ParseResult
import parser.Parser
import kotlin.test.assertEquals

class InterpreterTest {
    private val lexer: Lexer = LexerImpl()
    private val parser = Parser()

    @Test
    fun `test leaf int`() {
        testResult("12", "12")
    }

    @Test
    fun `test leaf string`() {
        testResult("\"foo\"", "\"foo\"")
    }

    @Test
    fun `test leaf true`() {
        testResult("#t", "#t")
    }

    @Test
    fun `test leaf false`() {
        testResult("#f", "#f")
    }

    @Test
    fun `test quoted list`() {
        testResult("`(foo bar baz)", "`(foo bar baz)")
    }

    @Test
    fun `test env function`() {
        testResult("(+ 1 2 3)", "6")
    }

    @Test
    fun `test if 1`() {
        testResult("(if #t 1 2)", "1")
    }

    @Test
    fun `test if 2`() {
        testResult("(if #f 3 (if #t `(Good choice) 2))", "`(Good choice)")
    }

    @Test
    fun `test while`() {
        testResult("""
            (let ((i 0))
                (while (< i 10)
                    (set i (+ i 1))
                )
                i
            )
        """.trimIndent(), "10")
    }

    @Test
    fun `test let`() {
        testResult("""
            (let ((i 10))
                i
            )
        """.trimIndent(), "10")
    }


    @Test
    fun `test let in next definition previous accessible`() {
        testResult("""
            (let ((i 10) (b (+ i 2)))
                b
            )
        """.trimIndent(), "12")
    }

    @Test
    fun `test nested let accesses closest scope`() {
        testResult("""
            (let ((i 10))
                (let ((i 5))
                    i
                )
            )
        """.trimIndent(), "5")
    }

    @Test
    fun `test let binding is unavailable later`() {
        testResult("""
            (let ((i 10))
                ()
            )
            i
        """.trimIndent(), "Interpreter [23, 24): No i found in env")
    }


    @Test
    fun `test set`() {
        testResult("""
            (let ((i 10))
                (set i 5)
                i
            )
        """.trimIndent(), "5")
    }

    @Test
    fun `test set of non existing var`() {
        testResult("""
            (set i 5)
        """.trimIndent(), "Interpreter [0, 9): Variable i not found")
    }


    @Test
    fun `test defn`() {
        testResult("""
            (defn sqr (x) (* x x))
            (sqr 12)
        """.trimIndent(), "144")
    }

    @Test
    fun `test macro expand data node`() {
        testResult("""
            (macro sqr (x) `(* x x))
            (sqr 12)
        """.trimIndent(), "(* 12 12)")
    }

    @Test
    fun `test macro expand same as defn`() {
        testResult("""
            (macro sqr (x) (* x x))
            (sqr 12)
        """.trimIndent(), "144")
    }

    private fun testResult(program: String, expectedResult: String) {
        val parseResult = parser.parse(lexer.tokenize(program))
        when (parseResult) {
            is ParseResult.Error -> assertEquals(expectedResult, parseResult.toString())
            is ParseResult.Ok -> {
                val result = try {
                    Interpreter().eval(parseResult.node).lispy()
                }  catch (e: InterpreterException) {
                    e.toString()
                }
                assertEquals(expectedResult, result)
            }
        }
    }
}