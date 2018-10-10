package util.collection

/**
 *
 */
interface BiMap<K, V> {
    fun getKeyByValue(v: V) : K?
    fun getValueByKey(k: K) : V?
    // TODO think, maybe better to return something else
    fun put(k: K, v: V): Boolean
    fun containsKey(k: K): Boolean
    fun containsValue(v: V): Boolean
    val keyToValueView: KeyToValueView<K, V>
        get() = KeyToValueView(this)
    val valueToKeyView: ValueToKeyView<K, V>
        get() = ValueToKeyView(this)
}

inline class KeyToValueView<K, V>(private val map: BiMap<K, V>) {
    operator fun get(k: K): V? = map.getValueByKey(k)
    operator fun contains(k: K): Boolean = map.containsKey(k)
}

inline class ValueToKeyView<K, V>(private val map: BiMap<K, V>) {
    operator fun get(v: V): K? = map.getKeyByValue(v)
    operator fun contains(v: V): Boolean = map.containsValue(v)
}