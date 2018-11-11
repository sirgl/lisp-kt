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
[0, 27)@File
  [-1, 2)@IntLiteral 144
        """.trim(), listOf(
                "main" withText "(macro m (x) (* x x))(m 12)"
        ))
    }

    @Test
    fun `test single file expansion data`() {
        testExpansion("""
main:
[0, 28)@File
  [14, 21)@List
    [15, 16)@Identifier *
    [25, 27)@IntLiteral 12
    [25, 27)@IntLiteral 12
        """, listOf(
                "main" withText "(macro m (x) `(* x x))(m 12)"
        ))
    }

    private fun testExpansion(expectedExpansion: String, files: List<InMemoryFileInfo>, targetIndex: Int = 0) {
        val asts = buildAsts(files)
        val expander = MacroExpander()
        val dependencyGraphBuilder = DependencyGraphBuilder(asts)
        val graph = (dependencyGraphBuilder.build() as ResultWithLints.Ok).value

        val resultWithLints = expander.expand(asts, targetIndex, graph[targetIndex])
        val actual = buildString {
            append(resultWithLints.lints.joinToString("\n") { it.toString() })
            append("\n")
            if (resultWithLints is ResultWithLints.Ok) {
                val newAsts = resultWithLints.value
                for ((index, file) in files.withIndex()) {
                    append(file.name)
                    append(":\n")
                    append(newAsts[index].root.prettyPrint())
                }
            }
        }.trim()
        assertEquals(expectedExpansion.trim(), actual)
    }
}