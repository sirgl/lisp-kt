package deps

import analysis.Matchers
import analysis.extractOrNull
import parser.Ast
import util.ResultWithLints
import java.util.*

sealed class DependencyEntry {
    abstract val dependencies: List<DependencyEntry>
}

class RealDependencyEntry(
        val ast: Ast,
        override val dependencies: List<DependencyEntry>
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


class DependencyGraphBuilder(val asts: List<Ast>) {
    fun buildDependencyGraph() : ResultWithLints<List<DependencyEntry>> {
        val nameToIndex = HashMap<String, Int>() // module name to index
        val dependencyMap = HashMap<Int, MutableList<String>>() // index of module to list of module names as dependencies
        for ((index, ast) in asts.withIndex()) {
            val root = ast.root
            val children = root.children
            val first = children.firstOrNull()
            val source = ast.source
            dependencyMap[index] = mutableListOf()
            var isFirst = true
            for (child in children) {
                if (isFirst) {
                    isFirst = false
                    val moduleInfo = Matchers.MODULE.extractOrNull(first, source)
                    if (moduleInfo != null) {
                        val name = moduleInfo.name
                        nameToIndex[name] = index
                        continue
                    }
                }
                val importInfo = Matchers.IMPORT.extractOrNull(child, source) ?: break
                val depList = dependencyMap[index] ?: mutableListOf()
                depList.add(importInfo.name)
                dependencyMap[index] = depList
            }
        }
        // Building graph out of facts about every module
        val dependencyList = asts.map { RealDependencyEntry(it, mutableListOf()) }
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

