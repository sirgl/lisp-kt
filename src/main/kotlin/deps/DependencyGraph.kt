package deps

import analysis.Matchers
import analysis.extractOrNull
import parser.Ast
import util.ResultWithLints
import java.util.*

sealed class DependencyEntry {
    abstract val dependencies: List<DependencyEntry>
}

/**
 * Expected, that no Unsatisfied dependencies present in graph
 * All indices must be aligned with initial ast list
 */
fun DependencyEntry.remapToNewAst(newAst: List<Ast>): List<RealDependencyEntry> {
    val nodeDependencies = hashMapOf<Int, List<Int>>()
    dfs { entry ->
        entry as RealDependencyEntry
        nodeDependencies[entry.index] = entry.dependencies.map { entryDeps -> (entryDeps as RealDependencyEntry).index }
    }
    val newDependencies = newAst
            .mapIndexed {index, ast -> RealDependencyEntry(ast, mutableListOf(), index)  }
    for (index in 0 until newAst.size) {
        val deps = nodeDependencies[index]!!
        val depList = newDependencies[index].dependencies as MutableList<DependencyEntry>
        for (depIndex in deps) {
            depList.add(newDependencies[depIndex])
        }
    }
    return newDependencies
}

class RealDependencyEntry(
        val ast: Ast,
        override val dependencies: List<DependencyEntry>,
        val index: Int // Index in all asts list
) : DependencyEntry() {
    override fun toString(): String {
        return ast.source.path
    }
}

class UnsatisfiedDependencyEntry(val name: String) : DependencyEntry() {
    override val dependencies: List<DependencyEntry>
        get() = emptyList()
}

fun DependencyEntry.bfs(f: (DependencyEntry) -> Unit) {
    val queue = ArrayDeque<DependencyEntry>()
    val passed = hashSetOf<DependencyEntry>()
    queue.add(this)
    while (queue.isNotEmpty()) {
        val entry = queue.poll()
        passed.add(entry)
        f(entry)
        for (dependency in entry.dependencies) {
            if (dependency in passed) continue
            queue.add(dependency)
        }
    }
}

fun DependencyEntry.preorderDfs(f: (DependencyEntry) -> Unit) {
    preorderDfs(f, hashSetOf())
}

private fun DependencyEntry.preorderDfs(f: (DependencyEntry) -> Unit, passed: MutableSet<DependencyEntry>)  {
    passed.add(this)
    f(this)
    for (dependency in dependencies) {
        if (dependency !in passed) {
            dependency.dfs(f, passed)
        }
    }
}

fun DependencyEntry.dfs(f: (DependencyEntry) -> Unit) {
    dfs(f, hashSetOf())
}

private fun DependencyEntry.dfs(f: (DependencyEntry) -> Unit, passed: MutableSet<DependencyEntry>)  {
    passed.add(this)
    for (dependency in dependencies) {
        if (dependency !in passed) {
            dependency.dfs(f, passed)
        }
    }
    f(this)
}


class DependencyGraphBuilder(val asts: List<Ast>, val implicitImports: List<String>) {
    fun build() : ResultWithLints<List<DependencyEntry>> {
        val nameToIndex = HashMap<String, Int>() // module name to index
        val dependencyMap = HashMap<Int, MutableList<String>>() // index of module to list of module names as dependencies
        for ((index, ast) in asts.withIndex()) {
            val root = ast.root
            val children = root.children
            val first = children.firstOrNull()
            val source = ast.source
            dependencyMap[index] = implicitImports.toMutableList()
            var isFirst = true
            for (child in children) {
                if (isFirst) {
                    isFirst = false
                    val moduleInfo = Matchers.MODULE.extractOrNull(first, source)
                    if (moduleInfo != null) {
                        val name = moduleInfo.name
                        if (name == "stdlib") {
                            dependencyMap[index]?.removeAll(implicitImports)
                        }
                        nameToIndex[name] = index
                        continue
                    }
                }
                val importInfo = Matchers.IMPORT.extractOrNull(child, source) ?: continue
                val depList = dependencyMap[index] ?: mutableListOf()
                depList.add(importInfo.name)
                dependencyMap[index] = depList
            }
        }
        // Building graph out of facts about every module
        val dependencyList = asts.mapIndexed { astIndex, ast -> RealDependencyEntry(ast, mutableListOf(), astIndex) }
        for ((index, dependencies) in dependencyMap) {
            val entry = dependencyList[index]
            val entryDependencies = entry.dependencies as MutableList<DependencyEntry>
            for (dependency in dependencies) {
                val dependencyIndex = nameToIndex[dependency]
                val dependencyToAdd = if (dependencyIndex == null) {
                    UnsatisfiedDependencyEntry(dependency)
                } else {
                    dependencyList[dependencyIndex]
                }
                entryDependencies.add(dependencyToAdd)
            }
        }
        return ResultWithLints.Ok(dependencyList)
    }
}

