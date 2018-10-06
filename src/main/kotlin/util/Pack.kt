package util

inline class LongStorage(val storage: Long) {
    constructor(first: Int, second: Int) : this(packIntoLong(first, second))

    val first: Int
        get() = (storage shr 32).toInt()
    val second: Int
        get() = storage.toInt()

    val a: Byte
        get() = TODO()
    val b: Byte
        get() = TODO()
    val c: Byte
        get() = TODO()
    val d: Byte
        get() = TODO()
    val e: Byte
        get() = TODO()
    val f: Byte
        get() = TODO()
    val g: Byte
        get() = TODO()
    val h: Byte
        get() = TODO()

    val ab: Short
        get() = TODO()
    val cd: Short
        get() = TODO()
    val ef: Short
        get() = TODO()
    val gh: Short
        get() = TODO()
}

private fun packIntoLong(first: Int, second: Int): Long {
    return first.toLong() shl 32 or (second.toLong() and 0xffffffffL)
}
