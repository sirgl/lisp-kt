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

    @Test
    fun `test let not expanded`() {
        testExpansion("""
main:
[0, 23)@File
  [0, 23)@List
    [1, 4)@Identifier let
    [5, 20)@List
      [6, 12)@List
        [7, 8)@Identifier x
        [9, 11)@IntLiteral 12
      [13, 19)@List
        [14, 15)@Identifier y
        [16, 18)@IntLiteral 22
    [21, 22)@Identifier x
        """, listOf(
                "main" withText "(let ((x 12) (y 22)) x)"
        ))
    }

    @Test
    fun `test macro`() {
        testExpansion("""
main:
[0, 47)@File
  [0, 16)@List
    [1, 5)@Identifier defn
    [6, 7)@Identifier *
    [8, 13)@List
      [9, 10)@Identifier x
      [11, 12)@Identifier y
    [14, 15)@IntLiteral 0
  [14, 15)@IntLiteral 0
        """, listOf(
                "main" withText "(defn * (x y) 0)(macro sqr (x) (* x x))(sqr 12)"
        ))
    }


    @Test
    fun `test macro 2`() {
        testExpansion("""
main:
[0, 180)@File
  [30, 49)@List
    [31, 35)@Identifier defn
    [36, 41)@Identifier print
    [42, 45)@List
      [43, 44)@Identifier x
    [46, 48)@List
  [50, 67)@List
    [51, 55)@Identifier defn
    [56, 57)@Identifier +
    [58, 63)@List
      [59, 60)@Identifier x
      [61, 62)@Identifier y
    [64, 66)@List
  [68, 85)@List
    [69, 73)@Identifier defn
    [74, 75)@Identifier <
    [76, 81)@List
      [77, 78)@Identifier x
      [79, 80)@Identifier y
    [82, 84)@List
  [86, 180)@List
    [87, 90)@Identifier let
    [91, 98)@List
      [92, 97)@List
        [93, 94)@Identifier i
        [95, 96)@IntLiteral 0
    [103, 178)@List
      [104, 109)@Identifier while
      [110, 118)@List
        [111, 112)@Identifier <
        [113, 114)@Identifier i
        [115, 117)@IntLiteral 10
      [127, 148)@List
        [128, 133)@Identifier print
        [21, 28)@List
          [22, 23)@Identifier *
          [144, 146)@IntLiteral 12
          [144, 146)@IntLiteral 12
      [157, 172)@List
        [158, 161)@Identifier set
        [162, 163)@Identifier i
        [164, 171)@List
          [165, 166)@Identifier +
          [167, 168)@Identifier i
          [169, 170)@IntLiteral 1
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
[0, 38)@File
  [16, 23)@List
    [17, 18)@Identifier *
    [16, 23)@List
      [17, 18)@Identifier *
      [34, 36)@IntLiteral 12
      [34, 36)@IntLiteral 12
    [16, 23)@List
      [17, 18)@Identifier *
      [34, 36)@IntLiteral 12
      [34, 36)@IntLiteral 12
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
                    append(newAsts[index].root.prettyPrint())
                }
            }
        }.trim()
        assertEquals(expectedExpansion.trim(), actual)
    }
}