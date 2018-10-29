package util

import java.io.File
import java.io.InputStream

interface Source {
    fun getInputStream() : InputStream

    val path: String
}

class FileSource(override val path: String) : Source {
    override fun getInputStream(): InputStream {
        return File(path).inputStream().buffered()
    }
}

class InMemorySource(val text: String) : Source {
    override fun getInputStream(): InputStream {
        return text.byteInputStream()
    }

    override val path: String
        get() = "<memory>"

}
