package query

import java.util.*
import kotlin.collections.HashMap

interface Database {
    fun <T> queryFor(descriptor: TypedKey<T>): T

    fun registerQuery(query: Query<*>)
}

// inputs are basic - it is what given for us
// using queries we can infer new information using basic inputs
// assumption: only one query can produce value with given descriptor, otherwise, many solutions possible
class DatabaseImpl(inputs: List<DatabaseValue<*>>) : Database {
    private val inputMap: Map<TypedKey<*>, DatabaseValue<*>> =
        inputs.associateBy({ it.descriptor.keys.first() }) { it }

    // map from output descriptor key to query, with such output descriptor key
    // answers question: what queries execution can lead to getting output with given descriptor
    private val queryDescriptorMap: MutableMap<TypedKey<*>, Query<*>> = hashMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <T> queryFor(descriptor: TypedKey<T>): T {
        val directBasicValue = inputMap[descriptor]
        if (directBasicValue != null) return directBasicValue.value as T
        val root = findSolution(descriptor)
        return executeQuery(root) as T

    }

    @Suppress("UNCHECKED_CAST")
    private fun executeQuery(node: SolutionNode): Any {
        return when (node) {
            is ValueSolutionNode -> node.value.value!!
            is QuerySolutionNode -> {
                val query = node.query as Query<Any>
                val descriptor = query.inputKey
                // Just
                val queryInput = when (descriptor) {
                    is TypedKey<*> -> hashMapOf(descriptor to executeQuery(node.dependencies.first()))
                    is MultiKey -> {
                        val argsStorage = HashMap<TypedKey<*>, Any>()
                        for ((index, key) in descriptor.descriptors.withIndex()) {
                            argsStorage[key] = executeQuery(node.dependencies[index])
                        }
                        argsStorage
                    }
                }
                query.doQuery(TypedStorage(queryInput))
            }
            // only one node is possible for root
            is RootSolutionNode -> executeQuery(node.dependencies.first())
        }
    }

    private fun <T> findSolution(descriptor: TypedKey<T>): RootSolutionNode {
        val root = RootSolutionNode(descriptor)
        val currentNodes = ArrayDeque<SolutionNode>()
        val handledNodes = hashSetOf<SolutionNode>()
        currentNodes.push(root)
        while (currentNodes.isNotEmpty()) {
            val currentNode = currentNodes.pop()
            handledNodes.add(currentNode)
            for (key in currentNode.keys) {
                val input = inputMap[key]
                if (input != null) {
                    currentNode.dependencies.add(ValueSolutionNode(key, input))
                } else {
                    val query = queryDescriptorMap[key]
                        ?: throw IllegalStateException("No query found to compute for '$key' descriptor")
                    val node = QuerySolutionNode(query.inputKey.keys, query)
                    if (node in handledNodes) {
                        throw IllegalStateException("Loop detected")
                    }
                    currentNodes.add(node)
                    currentNode.dependencies.add(node)
                }
            }
        }
        return root
    }

    override fun registerQuery(query: Query<*>) {
        queryDescriptorMap[query.outputDescriptor] = query
    }

    fun getGraphvizOfAllQueries(): String {
        return buildString {
            append("digraph Database {\n")
            for ((output, query) in queryDescriptorMap) {
                val queryName = query.name ?: query.javaClass.name
                append("\"$queryName\" [shape=box]\n")
                append("\"$queryName\" -> \"${output.name}\";\n")
                for (key in query.inputKey.keys) {
                    append("\"${key.name}\" -> \"$queryName\";\n")
                }
            }
            append("}")
        }
    }
}

private sealed class SolutionNode(
    val keys: List<ValueKey>,
    val dependencies: MutableList<SolutionNode> = mutableListOf()
)

private class QuerySolutionNode(
    keys: List<ValueKey>,
    val query: Query<*>,
    dependencies: MutableList<SolutionNode> = mutableListOf()
) : SolutionNode(keys, dependencies)

private class ValueSolutionNode(
    key: ValueKey,
    val value: DatabaseValue<*>
) : SolutionNode(listOf(key), mutableListOf())

private class RootSolutionNode(
    key: TypedKey<*>,
    dependencies: MutableList<SolutionNode> = mutableListOf()
) : SolutionNode(listOf(key), dependencies)

class DatabaseValue<T>(
    val descriptor: TypedKey<T>,
    val value: T
)