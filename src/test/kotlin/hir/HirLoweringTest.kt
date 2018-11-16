package hir

import MultifileAstBasedTest
import InMemoryFileInfo
import deps.DependencyEntry
import deps.DependencyGraphBuilder
import deps.remapToNewAst
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
    fun `test top level data`() {
        testHirLowering("""
main:
File
  Function declaration: (main) main__init
    Block expr
      List literal
        Identifier literal: while
        Bool literal: true
        Int literal: 1
        Int literal: 2
        """, listOf(
                "main" withText "`(while #t 1 2)"
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

    @Test
    fun `test native function`() {
        testHirLowering("""
main:
File
  Native function declaration: + (runtime name: __add)
    Parameter: x
    Parameter: y
  Function declaration: (main) main__init
    Block expr
      Expr stmt
        Function reference: +
      Local call: +
        Int literal: 1
        Int literal: 2
        """, listOf(
                "main" withText "(defnat + __add (x y))(+ 1 2)"
        ))
    }

    @Test
    fun `test top level macro`() {
        testHirLowering("""
main:
File
  Native function declaration: * (runtime name: r__mul)
    Parameter: x
    Parameter: y
  Function declaration: (main) main__init
    Block expr
      Expr stmt
        Function reference: *
      Int literal: 169
        """, listOf(
                "main" withText "(defnat * r__mul (x y))(macro sqr (x) (* x x))(sqr 13)"
        ))
    }


    @Test
    fun `test top level all features with mocked env`() {
        testHirLowering("""
main:
File
  Native function declaration: + (runtime name: r__add)
    Parameter: a
    Parameter: b
  Native function declaration: * (runtime name: r__mul)
    Parameter: a
    Parameter: b
  Native function declaration: < (runtime name: r__lt)
    Parameter: a
    Parameter: b
  Native function declaration: print (runtime name: r__print)
    Parameter: x
  Function declaration: (main) main__init
    Block expr
      Expr stmt
        Function reference: +
      Expr stmt
        Function reference: *
      Expr stmt
        Function reference: <
      Expr stmt
        Function reference: print
      Block expr
        Var decl: i
          Int literal: 0
        Block expr
          While expr
            Local call: <
              Var reference: i
              Int literal: 10
            Block expr
              Expr stmt
                Local call: print
                  Int literal: 256
              Assign expr: i
                Local call: +
                  Var reference: i
                  Int literal: 1
        """, listOf(
                "main" withText """
        (defnat + r__add (a b))
        (defnat * r__mul (a b))
        (defnat < r__lt (a b))
        (defnat print r__print (x))

        (macro stat-sqr (x) (* x x))

        (let ((i 0))
            (while (< i 10)
                (print (stat-sqr (stat-sqr 4)))
                (set i (+ i 1))
            )
        )
                """.trimIndent()
        ))
    }



    @Test
    fun `test call by variable`() {
        testHirLowering("""
main:
File
TODO !!!!
        """, listOf(
                "main" withText """
        (let ((f (defn foo () ()))) (f))
                """.trimIndent()
        ))
    }

    @Test
    fun `test vararg function`() {
        testHirLowering("""
main:
File
  Function declaration: foo
    Parameter: params (vararg)
    Block expr
      Int literal: 12
  Function declaration: (main) main__init
    Block expr
      Function reference: foo
        """, listOf(
                "main" withText """
        (defn foo (@params) 12)
                """.trimIndent()
        ))
    }


    @Test
    fun `test vararg function not last`() {
        testHirLowering("""
main : Error in Validation [10, 21) : Vararg parameter is allowed only on the last position
        """, listOf(
                "main" withText """
        (defn foo (@params a) 12)
                """.trimIndent()
        ))
    }

    @Test
    fun `test vararg parameter count`() {
        testHirLowering("""
main:
File
  Function declaration: foo
    Parameter: a
    Parameter: params (vararg)
    Block expr
      Int literal: 12
  Function declaration: (main) main__init
    Block expr
      Expr stmt
        Function reference: foo
      Local call: foo
        Int literal: 12
        Int literal: 33
        Int literal: 44
        """, listOf(
                "main" withText """
        (defn foo (a @params) 12)
        (foo 12 33 44)
                """.trimIndent()
        ))
    }

    @Test
    fun `test vararg of native function`() {
        testHirLowering("""
main:
File
  Native function declaration: pName (runtime name: runtimeName)
    Parameter: params (vararg)
  Function declaration: (main) main__init
    Block expr
      Function reference: pName
        """, listOf(
                "main" withText """
        (defnat pName runtimeName (@params))
                """.trimIndent()
        ))
    }

    @Test
    fun `test vararg parameter count lower`() {
        testHirLowering("""
main : Error in LoweringToHir [27, 30) : Parameter count and args count must match: foo
        """, listOf(
                "main" withText """
        (defn foo (a @params) 12)
        (foo)
                """.trimIndent()
        ))
    }

    @Test
    fun `test simple function parameter count`() {
        testHirLowering("""
main : Error in LoweringToHir [21, 24) : Parameter count and args count must match: foo
        """, listOf(
                "main" withText """
        (defn foo (a b) 12)
        (foo 3)
                """.trimIndent()
        ))
    }

    @Test
    fun `test native function parameter count`() {
        testHirLowering("""
main : Error in LoweringToHir [24, 27) : Parameter count and args count must match: foo
        """, listOf(
                "main" withText """
        (defnat foo foo (a b))
        (foo 3)
                """.trimIndent()
        ))
    }

    private fun testHirLowering(expectedHirPrint: String, files: List<InMemoryFileInfo>, targetIndex: Int = 0) {
        val asts = buildAsts(files)
        val expander = MacroExpander()
        val dependencyGraphBuilder = DependencyGraphBuilder(asts)
        val graph: List<DependencyEntry> = dependencyGraphBuilder.build().unwrap()
        val finalAsts = expander.expand(asts, graph[targetIndex]).unwrap()
        val newGraph = graph[targetIndex].remapToNewAst(finalAsts)
        val lowering = HirLowering(emptyList())
        val actual = buildString {
            val loweringResult = lowering.lower(newGraph[targetIndex])
            append(loweringResult.lints.joinToString("\n"))
            if (loweringResult.lints.isNotEmpty()) {
                append("\n")
            }
            if (loweringResult is ResultWithLints.Ok) {
                append(loweringResult.value.joinToString("\n\n") { it.source.path + ":\n" + it.pretty() })
            }
        }
        assertEquals(expectedHirPrint.trim(), actual.trim())
    }
}