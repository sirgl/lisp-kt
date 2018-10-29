package parser

import lexer.Lexer
import org.junit.jupiter.api.Test
import lexer.*
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
  [0, 2)@BoolLiteral #t
  [3, 5)@BoolLiteral #f
""")
    }

    // Handled in token validator
    @Test
    fun `atom parse int too long`() {
        testParse("12381289371287389127", """
[0, 20)@File
  [0, 20)@IntLiteral 12381289371287389127
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
    fun `nested list`() {
        testParse("(12 (12 22) 33)", """
[0, 15)@File
  [0, 15)@List
    [1, 3)@IntLiteral 12
    [4, 11)@List
      [5, 7)@IntLiteral 12
      [8, 10)@IntLiteral 22
    [12, 14)@IntLiteral 33
""")
    }

    @Test
    fun `list parse numbers`() {
        testParse("(12 22 33)", """
[0, 10)@File
  [0, 10)@List
    [1, 3)@IntLiteral 12
    [4, 6)@IntLiteral 22
    [7, 9)@IntLiteral 33
""")
    }

    @Test
    fun `if form`() {
        testParse("(if #t 22 33)", """
[0, 13)@File
  [0, 13)@List
    [1, 3)@Identifier if
    [4, 6)@BoolLiteral #t
    [7, 9)@IntLiteral 22
    [10, 12)@IntLiteral 33
""")
    }

    @Test
    fun `data node`() {
        testParse("`(if #t 22 33)", """
[0, 14)@File
  [0, 14)@Data
    [1, 14)@List
      [2, 4)@Identifier if
      [5, 7)@BoolLiteral #t
      [8, 10)@IntLiteral 22
      [11, 13)@IntLiteral 33
""")
    }

    private fun testParse(text: String, expected: String) {
        val tokens = lexer.tokenize(text)
        val result = parser.parse(tokens)
        assertEquals(expected.trim(), result.toString().trimEnd())
    }
}