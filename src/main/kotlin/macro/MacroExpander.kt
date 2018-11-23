package macro

import analysis.Matchers
import deps.*
import interpreter.*
import parser.*
import util.ResultWithLints
import util.Source


// Steps:
// Parsing all files (finding modules)
// Starting from the target one (implicit main function) starting bypass
// When meet import - go there first if not yet
// File bypass: trying to find in top level functions and macroses
// In target files: recursively visiting nodes
//   If top level -> try add nodes to namespace
//   If macro call -> create interpreter with env = stdlib env + global macro and defns. Then interpret call and replace node with result
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
        val macroEnv = hashMapOf<String, AstNode>()
        target.dfs {
            val realDependencyEntry = it as RealDependencyEntry
            val ast = realDependencyEntry.ast
            val source = ast.source
            newAsts[realDependencyEntry.index] = Ast(expandFileRecursive(ast.root, macroEnv, source), source)
        }
    }

    private fun expandWithContext(node: AstNode, macroEnv: MutableMap<String, AstNode>) : ExpansionResult<AstNode> {
        return when (node) {
            is LeafNode -> ExpansionResult(node, false)
            is ListNode -> expandList(node, macroEnv)
            is DataNode -> ExpansionResult(node, false)
            is FileNode -> throw IllegalStateException()
        }
    }

    private fun expandList(node: ListNode, macroEnv: MutableMap<String, AstNode>) : ExpansionResult<AstNode> {
        val children = node.children
        if (children.isNotEmpty()) {
            val nameNode = children.first() as? LeafNode
            if (nameNode != null) {
                val name = nameNode.token.text

                val maybeMacro = macroEnv[name]
                return if (maybeMacro != null && Matchers.MACRO.matches(maybeMacro)) {
                    val env = InterpreterEnv(macroEnv)
                    val newBody = Interpreter(env).eval(node)
                    ExpansionResult(newBody, true)
                } else {
                    buildListExpansion(children, macroEnv, node)
                }
            }
        }
        return buildListExpansion(children, macroEnv, node)
    }

    private fun buildListExpansion(children: List<AstNode>, macroEnv: MutableMap<String, AstNode>, node: ListNode): ExpansionResult<AstNode> {
        val childExpansions = children.map { expandWithContext(it, macroEnv) }
        val wasExpansion = childExpansions.any { it.wasExpansion }
        return ExpansionResult(ListNode(childExpansions.map { it.ast }, node.textRange), wasExpansion)
    }

    // TODO it can be done on more granular level rather than file
    private fun expandFileRecursive(root: FileNode, macroEnv: MutableMap<String, AstNode>, source: Source) : FileNode {
        var currentFile = root
        var expansionCount = 0
        while(true) {
            val expansionResult = expandFile(currentFile, macroEnv, source)
            if (!expansionResult.wasExpansion) return currentFile
            currentFile = expansionResult.ast
            expansionCount++
            if (expansionCount == 1000) throw IllegalStateException("Too deep macro")
        }
    }

    private fun expandFile(root: FileNode, macroEnv: MutableMap<String, AstNode>, source: Source): ExpansionResult<FileNode> {
        var wasExpansion = false
        val expandedChildren = root.children.mapNotNull {child ->
            when {
                Matchers.MACRO.matches(child, source) -> {
                    val macroResult = Matchers.MACRO.extract(child, source)
                    when (macroResult) {
                        is ResultWithLints.Ok -> {
                            val macroNode = macroResult.value
                            macroEnv[macroNode.name] = child
                            null
                        }
                        is ResultWithLints.Error -> child
                    }
                }
                Matchers.DEFN.matches(child, source) -> {
                    Matchers.DEFN.extract(child, source).ifPresent {
                        macroEnv[it.name] = child
                    }
                    child
                }
                Matchers.DEFNAT.matches(child, source) -> {
                    Matchers.DEFNAT.extract(child, source).ifPresent {
                        macroEnv[it.nameInProgram] = child
                    }
                    child
                }
                else -> {
                    val expansionResult = expandWithContext(child, macroEnv)
                    if (!wasExpansion) {
                        wasExpansion = expansionResult.wasExpansion
                    }
                    expansionResult.ast
                }
            }
        }
        return ExpansionResult(FileNode(expandedChildren, root.textRange), wasExpansion)
    }


    private class ExpansionResult<T: AstNode>(
            val ast: T,
            val wasExpansion: Boolean
    )
}