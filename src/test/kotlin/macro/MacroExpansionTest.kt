package macro

import InMemoryFileInfo
import MultifileAstBasedTest
import deps.DependencyGraphBuilder
import deps.RealDependencyEntry
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
    fun `test or chain macro`() {
        testExpansion("""
main:
(defnat _eq r__eq (a b))
(defnat first r__first (list))
(defnat tail r__tail (list))
(defnat size r__size (list))
(defn is-empty (list) (_eq (size list) 0))
(if (first (tail `(#f #f #f #t #f))) #t (if (first (tail (tail `(#f #f #f #t #f)))) #t (if (first (tail (tail (tail `(#f #f #f #t #f))))) #t (if (first (tail (tail (tail (tail `(#f #f #f #t #f)))))) #t #f))))
        """.trimIndent(), listOf("main" withText  """
            (defnat _eq r__eq (a b))
            ; List related functions
            (defnat first r__first (list))
            (defnat tail r__tail (list))
            (defnat size r__size (list))
            (defn is-empty (list) (_eq (size list) 0))

            (macro or-list (args)
                 (if (is-empty args)
                    #f
                    `(if (first args)
                        #t
                        (or-list (tail args))
                    )
                 )
            )

            (macro or (@args) (or-list args))
            (or #f #f #f #t #f)
        """.trimIndent()))
    }

    @Test
    fun `test while real example`() {
        testExpansion("""
main:
(defnat + r__add (a b))
(defnat _gt r__gt (a b))
(defnat _eq r__eq (a b))
(defn < (a b) (if (if (_gt a b) #f #t) (if (_eq a b) #f #t) #f))
(let ((i 0)) (while (< i 10) (set i (+ i 1))) i)
        """.trimIndent(), listOf("main" withText  """
            (defnat + r__add (a b))
            (defnat _gt r__gt (a b))
            (macro _and (a b) `(if a b #f))
            (defnat _eq r__eq (a b))
            (macro not (a) `(if a #f #t))
            (defn < (a b) (_and
                    (not (_gt a b))
                    (not (_eq a b))
                )
            )
            (let ((i 0))
                (while (< i 10)
                    (set i (+ i 1))
                )
                i
            )
        """.trimIndent()))
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


    @Test
    fun `test complex macro`() {
        testExpansion("""
main:
(defnat + r__add (a b))
(defnat < r__lt (a b))
(defnat print r__print (x))
(let ((i 0)) (while (< i 10) (print i) (+ i 1)))
        """, listOf(
                "main" withText """
(defnat + r__add (a b))
(defnat < r__lt (a b))
(defnat print r__print (x))

(macro inc (i) `(+ i 1))

(macro for (start end index body)
    `(let ((index start))
        (while (< index end)
            body
            (inc i)
        )
    )
)

(for 0 10 i (print i))
                """.trimIndent()
        ))
    }

    @Test
    fun `test macro with unknown function call`() {
        testExpansion("""
main : Error in MacroExpander [15, 22) : No definition of bar in env
        """, listOf(
                "main" withText """(macro foo (x) (bar x)) (foo 4)"""
        ))
    }

    @Test
    fun `test macro asm`() {
        testExpansion("""
(macro-asm-expanded foo "asm")
        """, listOf(
                "main" withText """(macroasm foo (let () (emit "asm1") (emit "asm2")))"""
        ))
    }

    private fun testExpansion(expectedExpansion: String, files: List<InMemoryFileInfo>, targetIndex: Int = 0) {
        val asts = buildAsts(files)
        val expander = MacroExpander()
        val dependencyGraphBuilder = DependencyGraphBuilder(asts, emptyList())
        val graph = (dependencyGraphBuilder.build() as ResultWithLints.Ok).value

        val dependencyEntry = graph[targetIndex]
        dependencyEntry as RealDependencyEntry
        val resultWithLints = expander.expand(asts, dependencyEntry)
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