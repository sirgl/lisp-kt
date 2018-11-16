package analysis

import lexer.TokenType
import linting.LintSink
import parser.AstNode
import parser.LeafNode
import parser.ListNode

/**
 * @param R - result type
 * @param C - context in handler
 */
class Handlers<R, C> {
    private val handlerMap = mutableMapOf<String, ListInfo<*, R, C>>()

    fun <T : NodeInfo> addHandler(name: String, matcher: ListMatcher<T>, handler: (T, C, AstNode)->R) {
        handlerMap[name] = ListInfo(matcher, handler)
    }

    @Suppress("UNCHECKED_CAST")
    fun map(node: AstNode, context: C, lintSink: LintSink): R? {
        if (node !is ListNode) return null
        val children = node.children
        val first = children.first() as? LeafNode ?: return null
        val firstToken = first.token
        if (firstToken.type != TokenType.Identifier) return null
        val listInfo = handlerMap[firstToken.text] ?: return null
        if (!listInfo.matcher.matches(node)) return null
        val nodeInfo = listInfo.matcher.extract(node).drainTo(lintSink) ?: return null
        val function = listInfo.handler as (NodeInfo, C, AstNode) -> R
        return function(nodeInfo, context, node)
    }

    private class ListInfo<T : NodeInfo, R, C>(
        val matcher: ListMatcher<T>,
        val handler: (T, C, AstNode) -> R
    )
}