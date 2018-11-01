package hir

import analysis.*
import analysis.Keywords.DEFN_KW
import analysis.Keywords.IF_KW
import analysis.Keywords.IMPORT_KW
import analysis.Keywords.LIBRARY_KW
import analysis.Keywords.WHILE_KW
import linting.Lint
import linting.Severity
import linting.Subsystem
import parser.*
import util.ResultWithLints
import util.Source

class HirLowering(val implicitImports: List<HirImport>) {
    fun lower(root: FileNode, source: Source): ResultWithLints<HirFile> {

        TODO()
    }

}

private class UnitHirLowering(val root: AstNode, val lowering: HirLowering, val source: Source) {
    var libraryName: String? = null
    val imports = mutableListOf<HirImport>()
    var state = State.LibraryOrImportOrFunction

    fun lower() : ResultWithLints<HirFile> {
        val children = root.children
        imports.addAll(lowering.implicitImports)
        var state = State.LibraryOrImportOrFunction
        val lints = mutableListOf<Lint>()
        for (child in children) {
            when (state) {
                State.LibraryOrImportOrFunction -> {
//                    val lintAndNextState = checkFirstTopLevel(child, source)
//                    if (!lintAndNextState.isError())
//                        lintAndNextState.lint?.let { lints.add(it) }
//                    state = lintAndNextState.nextState
                    TODO()
                }
                State.ImportOrFunction -> {
//                    if (!tryAddAsImport(child, imports)) {
//                        state = State.Functions
//                    }
                }
                State.Functions -> {

                }
            }
        }
        if (lints.any { it.severity == Severity.Error }) return ResultWithLints.Error(lints)
        TODO()
    }

    private fun handleResult(resultWithLints: ResultWithLints<TopLevelResult>) {

    }

    private fun checkFirstTopLevel(node: AstNode): ResultWithLints<TopLevelResult> {
//        val nextState = firstNodeMatchChain.matchThenHandle(node) ?: return TopLevelData(
//                State.LibraryOrImportOrFunction,
//                errorLint("Only library declaration, import or function definition are allowed as first element of file", source, node.textRange)
//        )
//        return TopLevelData(nextState)
        TODO()
    }

    private fun checkImportOrFunction(node: AstNode, source: Source): ResultWithLints<TopLevelResult> {
        val result = importOrFunctionMatchChain.matchThenHandle(node) ?: return ResultWithLints.Error(
                listOf(errorLint("After library declaration only import or function definition are allowed", source, node.textRange))
        )
        return ResultWithLints.Ok(result)
    }

    private fun checkFunction() {

    }

    private fun errorLint(text: String, source: Source, textRange: TextRange): Lint {
        return Lint("", textRange, Severity.Error, Subsystem.LoweringToHir, source)
    }

    companion object {
        private val libraryMatcher = firstMatches { it is LeafNode && it.token.text == LIBRARY_KW }
                .withNthElement(1) { it.syntaxKind == SyntaxKind.Identifier }


        private val importMatcher = firstMatches { it is LeafNode && it.token.text == IMPORT_KW }
                .withNthElement(1) { it.syntaxKind == SyntaxKind.Identifier }


        private val defnMatcher = firstMatches { it is LeafNode && it.token.text == DEFN_KW }
                .withNthElement(1) { it is ListNode }
                .withSizeRestriction { it > 2 }

        private val libraryMatcherToMapper: Pair<AstNodeMatcher, (AstNode) -> TopLevelResult> =
                libraryMatcher to { node -> TopLevelResult.Lib((node.children[1] as LeafNode).token.text) }

        private val importMatcherToMapper: Pair<AstNodeMatcher, (AstNode) -> TopLevelResult> =
                importMatcher to { node -> TopLevelResult.Lib((node.children[1] as LeafNode).token.text) }

        private val functionMatcherToMapper: Pair<AstNodeMatcher, (AstNode) -> TopLevelResult> =
                defnMatcher to { node -> TopLevelResult.Lib((node.children[1] as LeafNode).token.text) }


        private val firstNodeMatchChain = MatchChain(listOf(
                libraryMatcherToMapper,
                importMatcherToMapper,
                functionMatcherToMapper
        ))
        private val importOrFunctionMatchChain = MatchChain(listOf(
                importMatcherToMapper,
                functionMatcherToMapper
        ))






        val KEYWORDS = hashSetOf(IF_KW, IMPORT_KW, WHILE_KW, DEFN_KW)

    }
}


private sealed class TopLevelResult {
    class Func(val func: HirFunctionDefinition) : TopLevelResult()
    class Import(val import: HirImport) : TopLevelResult()
    class Lib(val libraryName: String) : TopLevelResult()
    object NoMatch : TopLevelResult()
}

private enum class State {
    LibraryOrImportOrFunction,
    ImportOrFunction,
    Functions
}
