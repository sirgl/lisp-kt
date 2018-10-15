package util.collection

class LongList(private var capacity: Int = 8): Iterable<Long> {
    override fun iterator(): Iterator<Long> {
        return internalStorage.asSequence().take(size).iterator()
    }

    var size: Int = 0
        private set(value) {
            field = value
        }
    private var internalStorage: LongArray = LongArray(capacity)

    operator fun get(index: Int) : Long {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("Was $index, but size = $size")
        return internalStorage[index]
    }

    fun add(value: Long) {
        if (size >= capacity) {
            increaseStorage()
        }
        internalStorage[size] = value
        size++
    }

    private fun increaseStorage() {
        val newLength = capacity * 2
        val newStorage = LongArray(newLength)
        System.arraycopy(internalStorage, 0, newStorage, 0, capacity)
        internalStorage = newStorage
        capacity = newLength
    }



    operator fun contains(value: Long) : Boolean {
        for (i in 0 until capacity) {
            if (internalStorage[i] == value) return true
        }
        return false
    }

    override fun toString(): String {
        return internalStorage.joinToString(", ")
    }
}

fun longListOf(vararg values: Long): LongList {
    val list = LongList(values.size)
    for (value in values) {
        list.add(value)
    }
    return list
}