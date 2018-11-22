package query


interface Query<O> {
    fun doQuery(input: TypedStorage) : O
    val outputDescriptor: TypedKey<O>
    val inputKey: ValueKey
    val name: String?
        get() = null
}

abstract class SimpleQuery<I, O>(override val name: String) : Query<O> {
    final override fun doQuery(input: TypedStorage): O = doQuery(input.get(inputKey))

    abstract fun doQuery(input: I) : O

    abstract override val inputKey: TypedKey<I>
}

sealed class ValueKey(val name: String) {
    abstract val keys: List<TypedKey<*>>
}

class TypedKey<T>(name: String) : ValueKey(name) {
    override val keys: List<TypedKey<*>>
        get() = listOf(this)
}

class MultiKey(name: String, val descriptors: List<TypedKey<*>>) : ValueKey(name) {
    override val keys: List<TypedKey<*>>
        get() = descriptors
}

class TypedStorage(private val map: Map<TypedKey<*>, Any>) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: TypedKey<T>) : T {
        return map[key] as T
    }
}