package macro

import analysis.Matchers
import analysis.extractOrNull
import parser.Ast
import util.ResultWithLints

// Import semantics: add symbols from global scope of import target to current file
// No library definition required!

// Steps:
// Parsing all files (finding libraries)
// Starting from the target one (implicit main function) starting bypass
// When meet import - go there first if not yet
// File bypass: trying to find in top level functions and macroses
// In target files: recursively visiting nodes
//   If top level -> try add nodes
//   If macro call -> create interpreter with env = default env + global macro and interpret call, then replace node with result
class MacroExpander {
    /**
     * @return list of ast with the same order
     */
    fun expand(asts: List<Ast>, targetIndex: Int) : ResultWithLints<List<Ast>> {
        return MacroExpansionContext(asts, targetIndex).expand()
    }
}

private class MacroExpansionContext(val asts: List<Ast>, val targetIndex: Int) {
    fun expand() : ResultWithLints<List<Ast>> {


        TODO()
    }


}

private class FileGraphVisitor(val asts: List<Ast>, val moduleMap: MutableMap<String, Int>) {
    fun visit(ast: Ast) {

    }
}
