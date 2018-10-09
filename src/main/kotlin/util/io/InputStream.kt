package util.io

interface InputStream {
    /**
     * @return -1 if end of file reached, else byte count read
     */
    fun read(buffer: ByteArray) : Int
}