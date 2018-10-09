package backend

import util.io.Path

/**
 * It is files and other information that we can get from backend as a result of compilation
 */
interface Artifact

// Not Java Path because of cross platform
interface FileArtifact : Artifact {
    /**
     * path relative from output directory
     */
    val path: Path
}

//class AssemblyFileArtifact(path: Path, val assembler: Assembler) : FileArtifact(path)