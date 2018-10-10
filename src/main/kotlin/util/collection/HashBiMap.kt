package util.collection

class HashBiMap<K, V> : BiMap<K, V> {
    private val keyToValue = HashMap<K, V>()
    private val valueToKey = HashMap<V, K>()

    override fun getKeyByValue(v: V): K? = valueToKey[v]

    override fun getValueByKey(k: K): V? = keyToValue[k]

    override fun put(k: K, v: V): Boolean {
        if (k in keyToValue || v in valueToKey) return false
        val previousValue = keyToValue.put(k, v)
        val previousKey = valueToKey.put(v, k)
        return true
    }

    override fun containsKey(k: K): Boolean = k in keyToValue

    override fun containsValue(v: V): Boolean = v in valueToKey
}