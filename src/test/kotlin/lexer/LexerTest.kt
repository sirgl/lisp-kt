package lexer

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LexerTest {
    val lexer = LexerImpl()

    @Test
    fun `test whitespaces`() {
        testLexer("  \t\n  \n", """
            7@End@""
        """)
    }

    @Test
    fun `test empty`() {
        testLexer("", """
            0@End@""
        """)
    }

    @Test
    fun `test pars`() {
        testLexer("())((", """
            0@Lpar@"("
            1@Rpar@")"
            2@Rpar@")"
            3@Lpar@"("
            4@Lpar@"("
            5@End@""
        """)
    }

    @Test
    fun `test identifiers`() {
        testLexer("foo", """
            0@Identifier@"foo"
            3@End@""
        """)
    }

    @Test
    fun `test identifiers with digits`() {
        testLexer("foo12 34bar", """
            0@Identifier@"foo12"
            6@Int@"34"
            8@Identifier@"bar"
            11@End@""
        """)
    }

    @Test
    fun `test function identifier`() {
        testLexer("(= a b)", """
            0@Lpar@"("
            1@Identifier@"="
            3@Identifier@"a"
            5@Identifier@"b"
            6@Rpar@")"
            7@End@""
        """)
    }

    @Test
    fun `test excl identifier`() {
        testLexer("(! a)", """
            0@Lpar@"("
            1@Identifier@"!"
            3@Identifier@"a"
            4@Rpar@")"
            5@End@""
        """)
    }

    @Test
    fun `test string literal`() {
        testLexer("\"foo\"", """
            0@String@""foo""
            5@End@""
        """)
    }

    @Test
    fun `test error`() {
        testLexer("@#$@#", """
            0@Error@"@#${'$'}@#"
            5@End@""
        """)
    }

    @Test
    fun `test error 2`() {
        testLexer("(foo@###@ abcd 123 &&&% )", """
            0@Lpar@"("
            1@Identifier@"foo"
            4@Error@"@###@"
            10@Identifier@"abcd"
            15@Int@"123"
            19@Error@"&&&%"
            24@Rpar@")"
            25@End@""
        """)
    }

    @Test
    fun `test bool literals`() {
        testLexer("#t #f sad", """
            0@TrueLiteral@"#t"
            3@FalseLiteral@"#f"
            6@Identifier@"sad"
            9@End@""
        """)
    }

    @Test
    fun `test one line comments`() {
        testLexer("#t ;;comment1\n15;", """
            0@TrueLiteral@"#t"
            14@Int@"15"
            17@End@""
        """)
    }

    @Test
    fun `test extended comment`() {
        testLexer("#t #|com|##f", """
            0@TrueLiteral@"#t"
            10@FalseLiteral@"#f"
            12@End@""
        """)
    }

    @Test
    fun `test extended comment with sharp in entry`() {
        testLexer("#t #|com#|#", """
            0@TrueLiteral@"#t"
            11@End@""
        """)
    }

    @Test
    fun `test extended comment at the end`() {
        testLexer("#|123|", """
            0@Error@"#|"
            2@Int@"123"
            5@Error@"|"
            6@End@""
        """)
    }

    @Test
    fun `test vararg`() {
        testLexer("12 @foo123 fa", """
            0@Int@"12"
            3@VarargIndentifier@"@foo123"
            11@Identifier@"fa"
            13@End@""
        """)
    }

    @Test
    fun `test vararg incomplete`() {
        testLexer("@", """
            0@Error@"@"
            1@End@""
        """)
    }

    private fun testLexer(text: String, expected: String) {
        val tokens = lexer.tokenize(text)
        // indents to make writing test more simple
        assertEquals(expected.trimEnd().substring(1), tokens.joinToString(separator = "\n") { "            $it" })
    }
}