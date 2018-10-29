package hir

import util.Source

// High level intermediate representation. Looks like AST for strongly typed languages
// Constructed after macro expansion


abstract class HirNode {
    abstract val children: List<HirNode>
}

abstract class HirLeafNode : HirNode() {
    override val children: List<HirNode>
        get() = emptyList()
}

class HirFile(
        val source: Source,
        val imports: List<HirImport>,
        val topLevelFunctions: List<HirFunctionDefinition>
) : HirNode() {
    override val children: List<HirNode> = childrenFrom(imports, topLevelFunctions)
}


class HirImport(val libraryName: String, val isExplicit: Boolean) : HirLeafNode()

// TODO type?
class HirParameter(
        val name: String
)  : HirLeafNode()

class HirFunctionDefinition(
        val name: String,
        val params: List<HirParameter>,
        val body: HirBlock
)

class HirBlock(
        val stmts: List<HirStmt>,
        val expr: HirExpr
) : HirNode() {
    override val children: List<HirNode>
        get() = childrenFrom(stmts, expr)

}

// Statements

sealed class HirStmt : HirNode()

class HirExprStmt(val expr: HirExpr) : HirStmt() {

    override val children: List<HirNode>
        get() = listOf(expr)
}

// adds to scope name
class HirVarDeclStmt(val name: String, val initializer: HirExpr) : HirStmt() {
    override val children: List<HirNode>
        get() = listOf(initializer)
}


class HirWhileStmt(val condition: HirExpr, val body: HirBlock) : HirStmt() {
    override val children: List<HirNode>
        get() = childrenFrom(condition, body)
}

class HirAssignStmt(val name: String, val initializer: HirExpr, val decl: HirVarDeclStmt) : HirStmt() {
    override val children: List<HirNode>
        get() = listOf(initializer)
}

// Expressions

sealed class HirExpr : HirNode()

class HirBlockExpr(val block: HirBlock) : HirExpr() {
    override val children: List<HirNode>
        get() = listOf(block)
}


sealed class HirCallExpr(val name: String, val args: List<HirExpr>) : HirExpr() {
    override val children: List<HirNode>
        get() = args
}

class HirGlobalCallExpr(
        name: String,
        args: List<HirExpr>
) : HirCallExpr(name, args)

class HirLocalCallExpr(
        name: String,
        args: List<HirExpr>,
        val definition: HirFunctionDefinition
) : HirCallExpr(name, args)

class HirIfExpr(
        val condition: HirExpr,
        val thenBranch: HirExpr,
        val elseBranch: HirExpr?
) : HirExpr() {
    override val children: List<HirNode>
        get() = childrenFrom(condition, thenBranch, elseBranch)
}

sealed class HirLiteralExpr : HirNode()

class HirBoolLiteral(val value: Boolean) : HirLiteralExpr() {
    override val children: List<HirNode>
        get() = emptyList()
}
class HirIntLiteral(val value: Int) : HirLiteralExpr() {
    override val children: List<HirNode>
        get() = emptyList()
}
class HirStringLiteral(val value: String) : HirLiteralExpr() {
    override val children: List<HirNode>
        get() = emptyList()
}

class HirListLiteral(val literals: List<HirLiteralExpr>) : HirLiteralExpr() {
    override val children: List<HirNode>
        get() = literals
}

class HirIdentifierLiteral(val name: String) : HirLiteralExpr() {
    override val children: List<HirNode>
        get() = emptyList()
}

// utils

private fun childrenFrom(vararg children: Any?): MutableList<HirNode> {
    val flatChildren = mutableListOf<HirNode>()
    for (child in children) {
        child ?: continue
        @Suppress("UNCHECKED_CAST")
        when (child) {
            is HirNode -> flatChildren.add(child)
            is List<*> -> flatChildren.addAll(child as Collection<HirNode>)
            else -> throw IllegalArgumentException("Bad child $child")
        }
    }
    return flatChildren
}
