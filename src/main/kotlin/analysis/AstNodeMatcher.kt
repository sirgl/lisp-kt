package analysis

import parser.AstNode

interface AstNodeMatcher {
    fun matches(node: AstNode) : Boolean
}

class NthElementMatcher(private val nodePredicate: (AstNode) -> Boolean, private val childIndex: Int) : AstNodeMatcher {
    override fun matches(node: AstNode): Boolean {
        val children = node.children
        if (children.size >= childIndex) return false
        val child = children[childIndex]
        return nodePredicate(child)
    }
}

class CompositeAstNodeMatcher(val matchers: List<AstNodeMatcher>) : AstNodeMatcher {
    override fun matches(node: AstNode): Boolean = matchers.all { it.matches(node) }
}

class SizeAstNodeMatcher(val sizePredicate: (Int) -> Boolean) : AstNodeMatcher {
    override fun matches(node: AstNode): Boolean = sizePredicate(node.children.size)
}

fun firstMatches(nodePredicate: (AstNode) -> Boolean) : AstNodeMatcher {
    return NthElementMatcher(nodePredicate, 0)
}

fun AstNodeMatcher.withSizeRestriction(sizePredicate: (Int) -> Boolean): AstNodeMatcher {
    return CompositeAstNodeMatcher(listOf(this, SizeAstNodeMatcher(sizePredicate)))
}

fun AstNodeMatcher.withSize(size: Int): AstNodeMatcher {
    return CompositeAstNodeMatcher(listOf(this, SizeAstNodeMatcher { it == size } ))
}

fun AstNodeMatcher.withNthElement(index: Int, nodePredicate: (AstNode) -> Boolean): AstNodeMatcher {
    return CompositeAstNodeMatcher(listOf(this, NthElementMatcher(nodePredicate, index)))
}

class MatchChain<T>(private val matcherToHandler: List<Pair<AstNodeMatcher, (AstNode) -> T>>) {
    fun matchThenHandle(node: AstNode) : T? {
        for ((matcher, handler) in matcherToHandler) {
            if (matcher.matches(node)) {
                return handler(node)
            }
        }
        return null
    }
}
