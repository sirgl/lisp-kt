package util.io

class Path(internal val internalPath: List<String>) {
    constructor(path: String) : this(path.split('\\', '/'))

    constructor(vararg paths: Path) : this(paths.flatMap { it.internalPath })

    override fun toString(): String = internalPath.joinToString("/")
}