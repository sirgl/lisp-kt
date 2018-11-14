package analysis

import lexer.TokenType
import linting.CollectingSink
import linting.HasErrorsSink
import linting.LintSink
import linting.Severity
import parser.AstNode
import parser.LeafNode
import parser.ListNode
import util.ResultWithLints
import util.Source



interface Validator {
    fun validate(node: AstNode, lintSink: LintSink, source: Source)
}

class ListMatcher<T : NodeInfo>(val name: String, val validator: Validator, val extractor: (AstNode) -> T) {
    fun matches(node: AstNode, source: Source): Boolean {
        if (node !is ListNode) return false
        val children = node.children
        val first = children[0] as? LeafNode ?: return false
        if (first.token.type != TokenType.Identifier) return false
        val firstText = first.token.text
        if (firstText != name) return false
        val errorSink = HasErrorsSink()
        validator.validate(node, errorSink, source)
        if (errorSink.hasErrors) return false
        return true
    }

    /**
     * Expects, that name exactly matches
     */
    fun extract(node: AstNode, source: Source) : ResultWithLints<T> {
        val sink = CollectingSink()
        validator.validate(node, sink, source)
        val lints = sink.lints
        return when {
            lints.any { it.severity == Severity.Error } -> ResultWithLints.Error(lints)
            else -> ResultWithLints.Ok(extractor(node), lints)
        }
    }
}

/**
 * Warning! Non error lints will be deleted
 */
fun <T : NodeInfo> ListMatcher<T>.extractOrNull(node: AstNode?, source: Source) : T? {
    if (node !is ListNode) return null
    val result = extract(node, source)
    return when (result) {
        is ResultWithLints.Ok -> result.value
        is ResultWithLints.Error -> null
    }
}