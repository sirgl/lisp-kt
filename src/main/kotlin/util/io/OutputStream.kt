package util.io

interface OutputStream {
    fun write(bytes: ByteArray) : Int
}