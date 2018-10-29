package hir

import parser.FileNode
import util.ResultWithLints

class HirLowering {
    fun lower(root: FileNode) : ResultWithLints<HirFile> {
        val children = root.children
        TODO()
    }
}