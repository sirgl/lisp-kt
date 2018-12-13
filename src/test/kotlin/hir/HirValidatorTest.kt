package hir

import FrontendTest
import InMemoryFileInfo
import frontend.CompilerConfig
import util.InMemorySource
import util.ResultWithLints
import withText
import kotlin.test.Test
import kotlin.test.assertEquals


class HirValidatorTest : FrontendTest(emptyList()) {
    @Test
    fun `test catch variable in let assignment`() {
        testHir("""
main : Error in LoweringToHir [0, 0) : The function should not capture variables from context: y
        """, listOf(
                "main" withText """
        (defn foo (x) (let ((y 5) (bar (defn bar (c) (set y c)))) (bar x)))
                """.trimIndent()
        ))
    }

    @Test
    fun `test catch variable in condition`() {
        testHir("""
main : Error in LoweringToHir [0, 0) : The function should not capture variables from context: x
        """, listOf(
                "main" withText """
        (defn foo (x) (x 5) (defn bar (c) (if x 5 6)))
                """.trimIndent()
        ))
    }

    private fun testHir(expected: String, files: List<InMemoryFileInfo>) {
        val sources = files.map { InMemorySource(it.text, it.name) }
        val session = frontend.compilationSession(sources, emptyList(), CompilerConfig(0), false)
        val resultWithLints = session.getHir()
        val actual: String = if (resultWithLints is ResultWithLints.Error) {
            resultWithLints.lints.joinToString("\n")
        } else {
            val hir = resultWithLints.unwrap()
            hir.joinToString("\n") { it.toString() }
        }
        assertEquals(expected.trim(), actual)
    }

}