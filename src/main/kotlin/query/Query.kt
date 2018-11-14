package query

interface Query<InputValue, OutputValue> {
    fun doQuery(input: InputValue) : OutputValue
    val outputDescriptor: SingleValueDescriptor<OutputValue>
    val inputDescriptor: ValueDescriptor<InputValue>
}

sealed class ValueDescriptor<T> {
    abstract fun keys() : List<String>
}

class SingleValueDescriptor<T>(val key: String) : ValueDescriptor<T>() {
    override fun keys(): List<String> = listOf(key)
}

/**
 * Mapper used only to assemble inputs before query. User can get only query output
 */
class MultiValueDescriptor<T>(val keys: List<String>, val mapper: (List<Any>) -> T) : ValueDescriptor<T>() {
    override fun keys(): List<String> = keys
}