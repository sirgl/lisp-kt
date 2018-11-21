package query

import java.util.*

interface Database {
    fun <T> queryFor(descriptor: SingleValueDescriptor<T>): T

    fun registerQuery(query: Query<*, *>)
}

// inputs are basic - it is what given for us
// using queries we can infer new information using basic inputs
// assumption: only one query can produce value with given descriptor, otherwise, many solutions possible
class DatabaseImpl(inputs: List<DatabaseValue<*>>) : Database {
    private val inputMap: Map<String, DatabaseValue<*>> = inputs.associateBy({it.descriptor.keys().first()}) { it }

    // map from output descriptor key to query, with such output descriptor key
    // answers question: what queries execution can lead to getting output with given descriptor
    private val queryDescriptorMap: MutableMap<String, Query<*,*>> = hashMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <T> queryFor(descriptor: SingleValueDescriptor<T>): T {
        val directBasicValue = inputMap[descriptor.key]
        if (directBasicValue != null) return directBasicValue.value as T
        val root = findSolution(descriptor)
        return executeQuery(root) as T

    }

    @Suppress("UNCHECKED_CAST")
    private fun  executeQuery(node: SolutionNode) : Any {
        return when (node) {
            is ValueSolutionNode -> node.value.value!!
            is QuerySolutionNode -> {
                val query = node.query as Query<Any, Any>
                val descriptor = query.inputDescriptor
                // Just
                val queryInput = when (descriptor) {
                    is SingleValueDescriptor -> executeQuery(node.dependencies.first())
                    is MultiValueDescriptor -> descriptor.mapper(node.dependencies.map { executeQuery(it) })
                }
                query.doQuery(queryInput)
            }
            // only one node is possible for root
            is RootSolutionNode -> executeQuery(node.dependencies.first())
        }
    }

    private fun <T> findSolution(descriptor: SingleValueDescriptor<T>): RootSolutionNode {
        val root = RootSolutionNode(descriptor.key)
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
                    val node = QuerySolutionNode(query.inputDescriptor.keys(), query)
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

    override fun registerQuery(query: Query<*, *>) {
        queryDescriptorMap[query.outputDescriptor.key] = query
    }

    fun getGraphvizOfAllQueries() : String {
        return buildString {
            append("digraph Database {\n")
            for ((output, query) in queryDescriptorMap) {
                append("\"${query.javaClass.name}\" -> \"$output\";\n")
                for (key in query.inputDescriptor.keys()) {
                    append("\"$key\" -> \"${query.javaClass.name}\";\n")
                }
            }
            append("}")
        }
    }
}

private sealed class SolutionNode(
        val keys: List<String>,
        val dependencies: MutableList<SolutionNode> = mutableListOf()
)

private class QuerySolutionNode(
        keys: List<String>,
        val query: Query<*, *>,
        dependencies: MutableList<SolutionNode> = mutableListOf()
) : SolutionNode(keys, dependencies)

private class ValueSolutionNode(
        key: String,
        val value: DatabaseValue<*>
) : SolutionNode(listOf(key), mutableListOf())

private class RootSolutionNode(
        key: String,
        dependencies: MutableList<SolutionNode> = mutableListOf()
) : SolutionNode(listOf(key), dependencies)

interface DatabaseValue<T> {
    val descriptor: ValueDescriptor<T>
    val value: T
}

class SimpleValue<T>(
        override val descriptor: ValueDescriptor<T>,
        override val value: T
) : DatabaseValue<T>