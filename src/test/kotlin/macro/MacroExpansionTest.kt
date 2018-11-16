package macro

import MultifileAstBasedTest
import InMemoryFileInfo
import deps.DependencyGraphBuilder
import parser.prettyPrint
import util.ResultWithLints
import withText
import kotlin.test.Test
import kotlin.test.assertEquals

class MacroExpansionTest : MultifileAstBasedTest() {
    @Test
    fun `test single file expansion`() {
        testExpansion("""
main:
(defnat * r__mul (x y))
144
        """.trim(), listOf(
                "main" withText "(defnat * r__mul (x y))(macro m (x) (* x x))(m 12)"
        ))
    }

    @Test
    fun `test single file expansion data`() {
        testExpansion("""
main:
(* 12 12)
        """, listOf(
                "main" withText "(macro m (x) `(* x x))(m 12)"
        ))
    }

    @Test
    fun `test let not expanded`() {
        testExpansion("""
main:
(let ((x 12) (y 22)) x)
        """, listOf(
                "main" withText "(let ((x 12) (y 22)) x)"
        ))
    }

    @Test
    fun `test macro 2`() {
        testExpansion("""
main:
(defn print (x) ())
(defn + (x y) ())
(defn < (x y) ())
(let ((i 0)) (while (< i 10) (print (* 12 12)) (set i (+ i 1))))
        """, listOf(
                "main" withText """
        (macro stat-sqr (x) `(* x x))
        (defn print (x) ())
        (defn + (x y) ())
        (defn < (x y) ())
        (let ((i 0))
            (while (< i 10)
                (print (stat-sqr 12))
                (set i (+ i 1))
            )
        )
                """.trimIndent()
        ))
    }



    @Test
    fun `test macro in macro `() {
        testExpansion("""
main:
(* (* 12 12) (* 12 12))
        """, listOf(
                "main" withText "(macro sqr (x) `(* x x))(sqr (sqr 12))"
        ))
    }



    // TODO check recursive macro
    // TODO

    private fun testExpansion(expectedExpansion: String, files: List<InMemoryFileInfo>, targetIndex: Int = 0) {
        val asts = buildAsts(files)
        val expander = MacroExpander()
        val dependencyGraphBuilder = DependencyGraphBuilder(asts)
        val graph = (dependencyGraphBuilder.build() as ResultWithLints.Ok).value

        val resultWithLints = expander.expand(asts, graph[targetIndex])
        val actual = buildString {
            append(resultWithLints.lints.joinToString("\n") { it.toString() })
            append("\n")
            if (resultWithLints is ResultWithLints.Ok) {
                val newAsts = resultWithLints.value
                for ((index, file) in files.withIndex()) {
                    append(file.name)
                    append(":\n")
                    append(newAsts[index].root.lispy())
                }
            }
        }.trim()
        assertEquals(expectedExpansion.trim(), actual)
    }
}