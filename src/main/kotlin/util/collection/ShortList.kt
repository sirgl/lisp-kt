package util.collection

class ShortList(private var capacity: Int = 8) {
    var size: Int = 0
        private set(value) {
            field = value
        }
    private var internalStorage: ShortArray = ShortArray(capacity)

    operator fun get(index: Int) : Short {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("Was $index, but size = $size")
        return internalStorage[index]
    }

    fun add(value: Short) {
        if (size >= capacity) {
            increaseStorage()
        }
        internalStorage[size] = value
        size++
    }

    private fun increaseStorage() {
        val newLength = capacity * 2
        val newStorage = ShortArray(newLength)
        System.arraycopy(internalStorage, 0, newStorage, 0, capacity)
        internalStorage = newStorage
        capacity = newLength
    }



    operator fun contains(value: Short) : Boolean {
        for (i in 0 until capacity) {
            if (internalStorage[i] == value) return true
        }
        return false
    }
}