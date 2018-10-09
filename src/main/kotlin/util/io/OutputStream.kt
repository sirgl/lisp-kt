package util.io

// TODO Writer / Reader?

interface OutputStream : AutoCloseable {
    fun write(bytes: ByteArray) : Int
}

fun OutputStream.writeAll(bytes: ByteArray) {
    var size = bytes.size
    while (size != 0) {
        val byteCount = write(bytes)
        if (byteCount == -1) return
        size -= byteCount
    }
}