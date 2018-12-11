package deps

import util.ResultWithLints
import withText
import kotlin.test.Test
import kotlin.test.assertEquals
import InMemoryFileInfo
import MultifileAstBasedTest

class DependencyTest : MultifileAstBasedTest() {

    @Test
    fun `test single dep`() {
        testDependencies("""
main
	lib
lib
        """.trim(), listOf(
                "main" withText "(module main)(import lib)",
                "lib" withText "(module lib)"
        ))
    }

    @Test
    fun `test 2 dep`() {
        testDependencies("""
main
	libA
	libB
libA
libB
	libA
        """.trim(), listOf(
                "main" withText "(module main)(import libA)(import libB)",
                "libA" withText "(module libA)",
                "libB" withText "(module libB)(import libA)"
        ))
    }

    @Test
    fun `test circular`() {
        testDependencies("""
main
	libA
libA
	libB
libB
	main
        """.trim(), listOf(
                "main" withText "(module main)(import libA)",
                "libA" withText "(module libA)(import libB)",
                "libB" withText "(module libB)(import main)"
        ))
    }

    @Test
    fun `test unsatisfied`() {
        testDependencies("""
main
	unknown-mod <unsatisfied>
        """.trim(), listOf(
                "main" withText "(module main)(import unknown-mod)"
        ))
    }

    private fun testDependencies(expectedDeps: String, files: List<InMemoryFileInfo>) {
        val asts = buildAsts(files)
        val dependencyGraphBuilder = DependencyGraphBuilder(asts, emptyList())
        val graph = dependencyGraphBuilder.build()
        val lintsText = graph.lints.joinToString { it.toString() }
        graph as ResultWithLints.Ok
        val actual = buildString {
            append(lintsText)
            for (dependencyEntry in graph.value) {
                dependencyEntry as RealDependencyEntry
                append(dependencyEntry.ast.source.path)
                append("\n")
                for (dependency in dependencyEntry.dependencies) {
                    append("\t")
                    when (dependency) {
                        is RealDependencyEntry -> append(dependency.ast.source.path)
                        is UnsatisfiedDependencyEntry -> append(dependency.name + " <unsatisfied>")
                    }
                    append("\n")
                }
            }
        }.trim()
        assertEquals(expectedDeps, actual)
    }
}

