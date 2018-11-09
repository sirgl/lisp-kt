package analysis

import lexer.TokenType
import linting.Lint
import linting.Severity
import linting.Subsystem
import parser.AstNode
import parser.LeafNode
import parser.ListNode
import util.Source

object Matchers {
    val DEFN = ListMatcher(Keywords.DEFN_KW, object: Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            val children = node.children
            if (children.size < 4) {
                lintSink.addError("Define node must have at least 4 children", node, source)
            }
            verifyName(children[1], "Name", source, lintSink)
            val parametersNode = children[2]
            if (parametersNode is ListNode) {
                val parameters = parametersNode.children
                for (parameter in parameters) {
                    verifyName(parameter, "Parameter", source, lintSink)
                }
            } else {
                lintSink.addLint(Lint(
                        "Parameters node must be a list node",
                        parametersNode.textRange,
                        Severity.Error,
                        Subsystem.Verification,
                        source
                ))
            }
        }
    }) { node ->
        val children = node.children
        val name = (children[1] as LeafNode).token.text
        val parameters = (children[2] as ListNode).children.map { (it as LeafNode).token.text }
        DefnNodeInfo(name, parameters, children.drop(3))
    }

    val MODULE = ListMatcher(Keywords.MODULE_KW, object: Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            verifyName(node.children[1], "Module", source, lintSink)
        }
    }) { node ->
        ModuleNodeInfo((node.children[1] as LeafNode).token.text)
    }

    val IMPORT = ListMatcher(Keywords.MODULE_KW, object: Validator {
        override fun validate(node: AstNode, lintSink: LintSink, source: Source) {
            verifyName(node.children[1], "Import", source, lintSink)
        }
    }) { node ->
        ImportNodeInfo((node.children[1] as LeafNode).token.text)
    }

    private fun LintSink.addError(text: String, node: AstNode, source: Source) {
        addLint(Lint(text, node.textRange, Severity.Error, Subsystem.Verification, source))
    }

    /**
     * @param nodeName starts from upper case
     */
    private fun verifyName(node: AstNode, nodeName: String, source: Source, lintSink: LintSink) {
        when (node) {
            is LeafNode -> if (node.token.type != TokenType.Identifier) {
                lintSink.addLint(Lint(
                        "$nodeName node must be leaf node",
                        node.textRange,
                        Severity.Error,
                        Subsystem.Verification,
                        source
                ))
            }
            else -> lintSink.addLint(Lint(
                    "$nodeName node must be leaf node",
                    node.textRange,
                    Severity.Error,
                    Subsystem.Verification,
                    source
            ))
        }
    }
}