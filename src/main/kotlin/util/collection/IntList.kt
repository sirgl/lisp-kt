package util.collection

class IntList(private var capacity: Int = 8) {
    var size: Int = 0
        private set(value) {
            field = value
        }
    private var internalStorage: IntArray = IntArray(capacity)

    operator fun get(index: Int) : Int {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("Was $index, but size = $size")
        return internalStorage[index]
    }

    fun add(value: Int) {
        if (size >= capacity) {
            increaseStorage()
        }
        internalStorage[size] = value
        size++
    }

    private fun increaseStorage() {
        val newLength = capacity * 2
        val newStorage = IntArray(newLength)
        System.arraycopy(internalStorage, 0, newStorage, 0, capacity)
        internalStorage = newStorage
        capacity = newLength
    }



    operator fun contains(value: Int) : Boolean {
        for (i in 0 until capacity) {
            if (internalStorage[i] == value) return true
        }
        return false
    }

    override fun toString(): String {
        return internalStorage.joinToString(", ")
    }
}