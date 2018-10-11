package util.io

// TODO think, how to make it relative
// Always path to leaf
class Path(internal val internalPath: List<String>) : Iterable<String> {
    override fun iterator(): Iterator<String> = internalPath.iterator()

    constructor(path: String) : this(path.split('\\', '/'))

    constructor(vararg paths: Path) : this(paths.flatMap { it.internalPath })

    override fun toString(): String = internalPath.joinToString("/")

    fun extension(): String {
        val last = lastOrNull() ?: return ""
        return last.substringAfterLast('.', "")
    }
}