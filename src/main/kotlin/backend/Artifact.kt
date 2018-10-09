package backend

abstract class Artifact

// Not Java Path because of cross platform
open class FileArtifact(val path: String) : Artifact()

class AssemblyFileArtifact(path: String, val assembler: Assembler) : FileArtifact(path)