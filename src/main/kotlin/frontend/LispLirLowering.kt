package frontend

import analysis.FirstElementMatcher
import linting.Lint
import lir.LirCompilationUnit
import lir.types.TypeStorage
import parser.AstNode
import parser.FileNode
import parser.LeafNode
import parser.ListNode
import util.ResultWithLints
import util.Source

class LispLirLowering(val typeStorage: TypeStorage = TypeStorage()) {
    // TODO lowering is also coupled with validation?
    fun lower(root: FileNode, source: Source) : ResultWithLints<LirCompilationUnit> {
//        val lints = mutableListOf<Lint>()
//        val topLevel = root.children
//        if (topLevel.isEmpty()) return ResultWithLints.Ok(LirCompilationUnit(source, listOf(), listOf(), typeStorage), lints)
//        val firstTopLevel = topLevel.first()
//        // TODO separate
//        val list = firstTopLevel as? ListNode
//        libraryMatcher.matches(list)
        TODO()
    }

    companion object {
        val libraryMatcher = FirstElementMatcher { first -> first is LeafNode && first.token.text == "library" }
    }
}