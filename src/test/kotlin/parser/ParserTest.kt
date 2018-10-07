package parser

import lexer.Lexer
import org.junit.jupiter.api.Test
import lexer.LexerImpl
import kotlin.test.assertEquals

class ParserTest {
    val parser = Parser()
    val lexer : Lexer = LexerImpl()

    @Test
    fun `atom parse identifier`() {
        testParse("foo", """
[0, 3)@File
  [0, 3)@Identifier foo
""")
    }

    @Test
    fun `atom parse int`() {
        testParse("12", """
[0, 2)@File
  [0, 2)@IntLiteral 12
""")
    }

    @Test
    fun `atom parse bool`() {
        testParse("#t #f", """
[0, 5)@File
  [0, 2)@BoolLiteral true
  [3, 5)@BoolLiteral false
""")
    }

    @Test
    fun `atom parse int too long`() {
        testParse("12381289371287389127", """
[0, 20)@File
  [0, 20)@Error Bad int: Number doesn't fit into integer token: 0@Int@"12381289371287389127"
""")
    }

    @Test
    fun `atom parse string literal`() {
        testParse("\"asdasd\"", """
[0, 8)@File
  [0, 8)@StringLiteral "asdasd"
""")
    }

    @Test
    fun `list parse numbers`() {
        testParse("(12 22 33)", """
[0, 10)@File
  [0, 10)@List
    [0, 1)@Service
    [1, 3)@IntLiteral 12
    [4, 6)@IntLiteral 22
    [7, 9)@IntLiteral 33
    [9, 10)@Service
""")
    }

    private fun testParse(text: String, expected: String) {
        val tokens = lexer.tokenize(text)
        val ast = parser.parse(tokens)
        assertEquals(expected.trim(), ast.prettyPrint().trimEnd())
    }
}