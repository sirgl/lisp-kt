package hir

import MultifileAstBasedTest
import InMemoryFileInfo
import deps.DependencyGraphBuilder
import macro.MacroExpander
import util.ResultWithLints
import withText
import kotlin.test.Test
import kotlin.test.assertEquals


class HirLoweringTest : MultifileAstBasedTest() {
    @Test
    fun `test top level`() {
        testHirLowering("""
main:
File
  Function declaration: foo
    Parameter: x
    Block expr
      Int literal: 12
  Function declaration: (main) main__init
    Block expr
      Function reference: foo
        """, listOf(
                "main" withText "(defn foo (x) 12)"
        ))
    }

    @Test
    fun `test parameter reference`() {
        testHirLowering("""
main:
File
  Function declaration: foo
    Parameter: x
    Block expr
      Var reference: x
  Function declaration: (main) main__init
    Block expr
      Function reference: foo
        """, listOf(
                "main" withText "(defn foo (x) x)"
        ))
    }

    @Test
    fun `test top level let`() {
        testHirLowering("""
main:
File
  Function declaration: (main) main__init
    Block expr
      Block expr
        Var decl: x
          Int literal: 12
        Var decl: y
          Int literal: 22
        Block expr
          Var reference: x
        """, listOf(
                "main" withText "(let ((x 12) (y 22)) x)"
        ))
    }

    @Test
    fun `test top level if`() {
        testHirLowering("""
main:
File
  Function declaration: (main) main__init
    Block expr
      If expr
        Bool literal: true
        Int literal: 1
        Int literal: 2
        """, listOf(
                "main" withText "(if #t 1 2)"
        ))
    }

    @Test
    fun `test top level while`() {
        testHirLowering("""
main:
File
  Function declaration: (main) main__init
    Block expr
      While expr
        Bool literal: true
        Block expr
          Expr stmt
            Int literal: 1
          Int literal: 2
        """, listOf(
                "main" withText "(while #t 1 2)"
        ))
    }

    @Test
    fun `test top level call`() {
        testHirLowering("""
main:
File
  Function declaration: foo
    Parameter: x
    Block expr
      Int literal: 12
  Function declaration: (main) main__init
    Block expr
      Expr stmt
        Function reference: foo
      Local call: foo
        Int literal: 4
        """, listOf(
                "main" withText "(defn foo (x) 12) (foo 4)"
        ))
    }

    private fun testHirLowering(expectedHirPrint: String, files: List<InMemoryFileInfo>, targetIndex: Int = 0) {
        val asts = buildAsts(files)
        val expander = MacroExpander()
        val dependencyGraphBuilder = DependencyGraphBuilder(asts)
        val graph = dependencyGraphBuilder.build().unwrap()
        val finalAsts = expander.expand(asts, graph[targetIndex]).unwrap()
        val lowering = HirLowering(emptyList())
        val actual = buildString {
            for (finalAst in finalAsts) {
                append(finalAst.source.path + ":" + "\n")
                // TODO bypass in correct order
                val loweringResult = lowering.lower(finalAst.root, finalAst.source, LoweringContext())
                append(loweringResult.lints.joinToString("\n"))
                if (loweringResult.lints.isNotEmpty()) {
                    append("\n")
                }
                if (loweringResult is ResultWithLints.Ok) {
                    append(loweringResult.value.pretty())
                }
            }
        }
        assertEquals(expectedHirPrint.trim(), actual.trim())
    }
}