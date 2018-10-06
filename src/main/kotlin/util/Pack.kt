package util

inline class LongStorage(val storage: Long) {
    constructor(first: Int, second: Int) : this(packIntoLong(first, second))
    constructor(ab: Short, bc: Short, de: Short, ef: Short) : this(packIntoLong(ab, bc, de, ef))
    constructor(ab: Short, cd: Short, efgh: Int) : this(packIntoLong(ab, cd, efgh))

    // TODO make constructor with 4 bytes and 2 shorts

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
        get() = (first shr 16).toShort()
    val cd: Short
        get() = first.toShort()
    val ef: Short
        get() = (second shr 16).toShort()
    val gh: Short
        get() = second.toShort()
}

private fun packIntoLong(first: Int, second: Int): Long {
    return first.toLong() shl 32 or (second.toLong() and 0xffffffffL)
}

// TODO probably for performance it should be better to avoid packToInt
private fun packIntoLong(ab: Short, bc: Short, de: Short, ef: Short): Long {
    return packIntoLong(packIntoInt(ab, bc), packIntoInt(de, ef))
}

private fun packIntoInt(first: Short, second: Short) : Int {
    return first.toInt() shl 16 or second.toInt()
}

private fun packIntoLong(ab: Short, cd: Short, efgh: Int) : Long {
    return packIntoLong(packIntoInt(ab, cd), efgh)
}