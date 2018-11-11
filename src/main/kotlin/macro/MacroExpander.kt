package macro

import analysis.Matchers
import deps.*
import interpreter.*
import parser.*
import util.ResultWithLints
import util.Source

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
    fun expand(asts: List<Ast>, target: DependencyEntry) : ResultWithLints<List<Ast>> {
        return MacroExpansionContext(asts, target).expand()
    }
}

// At this point all dependencies must be validated
private class MacroExpansionContext(asts: List<Ast>, val target: DependencyEntry) {
    val newAsts = asts.toMutableList()

    fun expand() : ResultWithLints<List<Ast>> {
        expandRecursive(target)
        return ResultWithLints.Ok(newAsts)
    }

    fun expandRecursive(node: DependencyEntry) {
        node as RealDependencyEntry
        val macroEnv = hashMapOf<String, EnvironmentEntry>()
        macroEnv.putAll(standardEnvFunctions)
        target.dfs {
            val realDependencyEntry = it as RealDependencyEntry
            val ast = realDependencyEntry.ast
            val source = ast.source
            newAsts[realDependencyEntry.index] = Ast(expandFile(ast.root, macroEnv, source), source)
        }
    }

    private fun expandWithContext(node: AstNode, macroEnv: MutableMap<String, EnvironmentEntry>) : AstNode {
        return when (node) {
            is LeafNode -> node
            is ListNode -> expandList(node, macroEnv)
            is DataNode -> node
            is FileNode -> throw IllegalStateException()
        }
    }

    private fun expandList(node: ListNode, macroEnv: MutableMap<String, EnvironmentEntry>) : AstNode {
        val children = node.children
        if (children.isNotEmpty()) {
            val nameNode = children.first() as? LeafNode
            if (nameNode != null) {
                val name = nameNode.token.text
                return if (macroEnv[name] is Macro) {
                    val env = InterpreterEnv(macroEnv)
                    Interpreter(env).eval(node)
                } else {
                    node
                }

            }
        }
        return ListNode(children.map { expandWithContext(it, macroEnv) }, node.textRange)
    }

    private fun expandFile(root: FileNode, macroEnv: MutableMap<String, EnvironmentEntry>, source: Source): FileNode {
        val expandedChildren = root.children.mapNotNull {child ->
            when {
                Matchers.MACRO.matches(child, source) -> {
                    val macroResult = Matchers.MACRO.extract(child, source)
                    when (macroResult) {
                        is ResultWithLints.Ok -> {
                            val macroNode = macroResult.value
                            macroEnv[macroNode.name] = Macro(macroNode.name, macroNode.parameters, macroNode.body)
                            null
                        }
                        is ResultWithLints.Error -> child
                    }
                }
                Matchers.DEFN.matches(child, source) -> {
                    val defnResult = Matchers.DEFN.extract(child, source)
                    defnResult.ifPresent {
                        macroEnv[it.name] = AstFunction(it.name, it.parameters, it.body)
                    }
                    child
                }
                else -> {
                    expandWithContext(child, macroEnv)
                }
            }
        }
        return FileNode(expandedChildren, root.textRange)
    }

}

private class FileGraphVisitor(val asts: List<Ast>, val moduleMap: MutableMap<String, Int>) {
    fun visit(ast: Ast) {

    }
}
