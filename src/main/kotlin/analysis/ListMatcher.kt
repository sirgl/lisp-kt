package analysis

import parser.AstNode
import parser.ListNode

interface ListMatcher {
    fun matches(list: ListNode) : Boolean
}

class FirstElementMatcher(private val firstNodePredicate: (AstNode) -> Boolean) : ListMatcher {
    override fun matches(list: ListNode): Boolean {
        val children = list.children
        val first = children.firstOrNull() ?: return false
        return firstNodePredicate(first)
    }
}