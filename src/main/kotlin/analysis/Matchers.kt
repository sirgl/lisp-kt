package analysis

import lexer.TokenType
import linting.Lint
import linting.LintSink
import linting.Severity
import linting.Subsystem
import parser.AstNode
import parser.LeafNode
import parser.ListNode
import util.Source

object Matchers {
    private var uniqueFnIdentifier: Int = 0

    class FunctionLikeValidator(private val nodeType: String) : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            val children = node.children
            if (!verifyCountAtLeast(node, nodeType, 3, source, lintSink)) return
            verifyName(children[1], "Name", source, lintSink)
            val parametersNode = children[2]
            verifyParametersNode(parametersNode, source, lintSink)
        }
    }

    class LambdaFunctionValidator(private val nodeType: String) : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            val children = node.children
            if (!verifyCountAtLeast(node, nodeType, 3, source, lintSink)) return
            val parametersNode = children[1]
            verifyParametersNode(parametersNode, source, lintSink)
        }
    }

    class CompositeValidator(private val validators: Map<String, Validator>) : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            var isMatched: Boolean = false
            val name = (node.children.firstOrNull() as? LeafNode)?.token?.text ?: return
            validators[name]?.validate(node, lintSink, source)
        }

    }

    private fun verifyParametersNode(parametersNode: AstNode, source: Source, lintSink: LintSink) {
        if (parametersNode is ListNode) {
            val parameters = parametersNode.children
            for ((index, parameter) in parameters.withIndex()) {
                if ((parameter as? LeafNode)?.token?.type == TokenType.Identifier) {
                    verifyName(parameter, "Parameter", source, lintSink)
                } else {
                    if (index != parameters.lastIndex) {
                        lintSink.addError("Vararg parameter is allowed only on the last position", parametersNode, source)
                    }
                }
            }
        } else {
            lintSink.addLint(
                Lint(
                    "Parameters node must be a list node",
                    parametersNode.textRange,
                    Severity.Error,
                    Subsystem.Validation,
                    source
                )
            )
        }
    }

    private val functionValidator = FunctionLikeValidator("Define")
    private val macroValidator = FunctionLikeValidator("Macro")

    val DEFN = ListMatcher(listOf(Keywords.DEFN_KW, Keywords.FN_KW),
            CompositeValidator(mapOf(
                    Keywords.DEFN_KW to functionValidator,
                    Keywords.FN_KW to LambdaFunctionValidator("LambdaFunction")
            ))
    ) { node ->
        val children = node.children
        val keyWord = (children[0] as LeafNode).token.text
        var name = ""
        val childCount: Int

        return@ListMatcher if (keyWord == Keywords.FN_KW) {
            uniqueFnIdentifier++
            name = "@uniqueId$uniqueFnIdentifier"
            val parameters = parseParameterList(children[1])
            DefnNodeInfo(name, parameters, children.drop(2))
        } else {
            name = (children[1] as LeafNode).token.text
            val parameters = parseParameterList(children[2])
            DefnNodeInfo(name, parameters, children.drop(3))
        }
    }

    val MACRO = ListMatcher(Keywords.MACRO_KW, macroValidator) { node ->
        val children = node.children
        val name = (children[1] as LeafNode).token.text
        val parameters = parseParameterList(children[2])
        MacroNodeInfo(name, parameters, children.drop(3))
    }

    val MACROASM = ListMatcher(Keywords.MACROASM_KW, object : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            val children = node.children
            if (!verifyCountExact(node, "MacroAsm", 3, source, lintSink)) return
            verifyName(children[1], "MacroAsm", source, lintSink)
        }

    }) { node ->
        val children = node.children
        val name = (children[1] as LeafNode).token.text
        MacroAsmNodeInfo(name, children[2])
    }

    val EMIT = ListMatcher(Keywords.EMIT_KW, object : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            if (!verifyCountExact(node, "Emit", 2, source, lintSink)) return
        }
    }) { node ->
        val children = node.children
        EmitNodeInfo((children[1] as LeafNode).token.text)
    }

    private fun parseParameterList(node: AstNode) : List<ParameterInfo> {
        node as ListNode
        val children = node.children
        val size = children.size
        val parameters = mutableListOf<ParameterInfo>()
        for (i in (0 until size)) {
            val current = children[i] as LeafNode
            if (i == size - 1) { // last may be vararg
                if (current.token.type == TokenType.VarargIndentifier) {
                    parameters.add(ParameterInfo(current.token.text.substring(1), true))
                    break
                }
            }
            parameters.add(ParameterInfo(current.token.text, false))
        }
        return parameters
    }

    val MODULE = ListMatcher(Keywords.MODULE_KW, object : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            if (!verifyCountExact(node, "Module", 2, source, lintSink)) return
            verifyName(node.children[1], "Module", source, lintSink)
        }
    }) { node ->
        ModuleNodeInfo((node.children[1] as LeafNode).token.text)
    }

    val IMPORT = ListMatcher(Keywords.IMPORT_KW, object : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            if (!verifyCountExact(node, "Import", 2, source, lintSink)) return
            verifyName(node.children[1], "Import", source, lintSink)
        }
    }) { node ->
        ImportNodeInfo((node.children[1] as LeafNode).token.text)
    }


    val LET = ListMatcher(Keywords.LET_KW, object : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            if (!verifyCountAtLeast(node, "Let", 3, source, lintSink)) return
            val children = node.children
            val declarationsNode = children[1]
            if (declarationsNode !is ListNode) {
                lintSink.addError("Declarations node in let must be a list node", declarationsNode, source)
                return
            }
            for (declaration in declarationsNode.children) {
                if (declaration !is ListNode) {
                    lintSink.addError("Single declaration in let must be a list node", declarationsNode, source)
                    return
                }
                if (!verifyCountExact(declaration, "Let declaration", 2, source, lintSink)) return
                val declarationChildren = declaration.children
                val declarationName = declarationChildren[0]
                verifyName(declarationName, "Let declaration name", source, lintSink)
            }
        }
    }) { node ->
        val children = node.children
        val body = children.drop(2)
        val declarations = children[1] as ListNode
        val decls = declarations.children.map { LetDecl((it.children[0] as LeafNode).token.text, it.children.last()) }
        LetNodeInfo(decls, body)
    }

    val IF = ListMatcher(Keywords.IF_KW, object : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            val children = node.children
            val childrenCount = children.size
            if (childrenCount != 3 && childrenCount != 4) {
                lintSink.addError("If must have 3 or 4 children", node, source)
            }
        }
    }) { node ->
        val children = node.children
        val condition = children[1]
        val thenBranch = children[2]
//        val elseBranch = children[3]
        when (children.size) {
            3 -> IfNodeInfo(condition, thenBranch, null)
            4 -> IfNodeInfo(condition, thenBranch, children[3])
            else -> throw IllegalStateException()
        }
    }

    val WHILE = ListMatcher(Keywords.WHILE_KW, object : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            if (!verifyCountAtLeast(node, "While", 3, source, lintSink)) return
        }
    }) { node ->
        val children = node.children
        val condition = children[1]
        WhileNodeInfo(condition, children.drop(2))
    }

    val SET = ListMatcher(Keywords.SET_KW, object : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            if (!verifyCountExact(node, "Set", 3, source, lintSink)) return
            verifyName(node.children[1], "Name", source, lintSink)
        }
    }) { node ->
        val children = node.children
        val name = (children[1] as LeafNode).token.text
        SetNodeInfo(name, children[2])
    }

    val DEFNAT = ListMatcher(Keywords.DEFNAT_KW, object : Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            if (!verifyCountExact(node, "Native function declaration", 4, source, lintSink)) return
            verifyName(node.children[1], "Runtime name", source, lintSink)
            verifyName(node.children[2], "Name in program", source, lintSink)
            verifyParametersNode(node.children[3], source, lintSink)
        }
    }) { node ->
        val children = node.children
        val runtimeName = (children[1] as LeafNode).token.text
        val programName = (children[2] as LeafNode).token.text
        NativeFunctionDeclarationInfo(runtimeName, programName, parseParameterList(children[3]))
    }


    private fun LintSink.addError(text: String, node: AstNode, source: Source) {
        addLint(Lint(text, node.textRange, Severity.Error, Subsystem.Validation, source))
    }

    /**
     * @param nodeName starts from upper case
     */
    private fun verifyName(node: AstNode, nodeName: String, source: Source, lintSink: LintSink) {
        when (node) {
            is LeafNode -> if (node.token.type != TokenType.Identifier) {
                lintSink.addLint(
                    Lint(
                        "$nodeName node must be identifier",
                        node.textRange,
                        Severity.Error,
                        Subsystem.Validation,
                        source
                    )
                )
            }
            else -> lintSink.addLint(
                Lint(
                    "$nodeName node must be leaf node",
                    node.textRange,
                    Severity.Error,
                    Subsystem.Validation,
                    source
                )
            )
        }
    }

    private fun verifyCountExact(
        node: AstNode,
        nodeName: String,
        count: Int,
        source: Source,
        lintSink: LintSink
    ): Boolean {
        if (node.children.size != count) {
            lintSink.addError("$nodeName must have $count children", node, source)
            return false
        }
        return true
    }

    private fun verifyCountAtLeast(node: AstNode, nodeName: String, count: Int, source: Source, lintSink: LintSink): Boolean {
        if (node.children.size < count) {
            lintSink.addError("$nodeName must have at least $count children", node, source)
            return false
        }
        return true
    }
}