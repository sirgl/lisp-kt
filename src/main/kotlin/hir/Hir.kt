package hir

import util.Source

// High level intermediate representation. Looks like AST for strongly typed languages
// Constructed after macro expansion

/**
 * Marker interface for node, that represents declaration in code
 */
interface HirDeclaration {
    val name: String
}


abstract class HirNode {
    abstract val children: List<HirNode>

    abstract fun prettySelf(): String
}

fun HirNode.pretty(): String {
    val sb = StringBuilder()
    pretty(sb, 0)
    return sb.toString()
}

private fun HirNode.pretty(sb: StringBuilder, level: Int) {
    for (i in (0 until level)) {
        sb.append("  ")
    }
    sb.append(prettySelf())
    sb.append("\n")
    for (child in children) {
        child.pretty(sb, level + 1)
    }
}

abstract class HirLeafNode : HirNode() {
    override val children: List<HirNode>
        get() = emptyList()
}

class HirFile(
        val source: Source,
        val imports: List<HirImport>,
        val functions: List<HirFunctionDeclaration>,
        val moduleName: String?
) : HirNode() {
    override val children: List<HirNode> = childrenFrom(imports, functions)

    override fun prettySelf(): String {
        return if (moduleName == null) "File" else "File: $moduleName"
    }
}


class HirImport(val moduleName: String, val isExplicit: Boolean) : HirLeafNode() {
    override fun prettySelf(): String {
        return "Import: $moduleName"
    }
}

interface HirVarDeclaration : HirDeclaration {
}

class HirParameter(
        override val name: String,
        val isVararg: Boolean = false
) : HirLeafNode(), HirVarDeclaration {
    override fun prettySelf(): String {
        return buildString {
            append("Parameter: $name")
            if (isVararg) {
                append(" (vararg)")
            }
        }
    }
}

interface HirFunctionDeclaration : HirDeclaration {
    val parameters: List<HirParameter>

    fun hasVarargs(): Boolean {
        return parameters.lastOrNull()?.isVararg ?: false
    }
}

class HirNativeFunctionDeclaration(
        override val name: String,
        val runtimeName: String,
        override val parameters: List<HirParameter>
) : HirNode(), HirFunctionDeclaration {
    override val children: List<HirNode>
        get() = emptyList()

    override fun prettySelf(): String {
        return "Native function declaration: $name (runtime name: $runtimeName)"
    }
}

class HirFunctionDefinition(
        override val name: String,
        override val parameters: List<HirParameter>,
        val body: HirBlockExpr, // TODO make optional to make it possible to make forward declarations
        val isMain: Boolean = false
) : HirNode(), HirFunctionDeclaration {
    override val children: List<HirNode>
        get() = childrenFrom(parameters, body)

    override fun prettySelf(): String {
        return buildString {
            append("Function declaration: ")
            if (isMain) {
                append("(main) ")
            }
            append(name)
        }
    }

}

class HirBlockExpr(
        val stmts: List<HirStmt>,
        val expr: HirExpr
) : HirExpr() {
    override val children: List<HirNode>
        get() = childrenFrom(stmts, expr)

    override fun prettySelf(): String {
        return "Block expr"
    }
}

// Statements

sealed class HirStmt : HirNode()

class HirExprStmt(val expr: HirExpr) : HirStmt() {

    override val children: List<HirNode>
        get() = listOf(expr)

    override fun prettySelf(): String {
        return "Expr stmt"
    }
}

// adds to scope name
class HirVarDeclStmt(override val name: String, val initializer: HirExpr) : HirStmt(), HirVarDeclaration {
    override val children: List<HirNode>
        get() = listOf(initializer)

    override fun prettySelf(): String {
        return "Var decl: $name"
    }
}


class HirWhileExpr(val condition: HirExpr, val body: HirBlockExpr) : HirExpr() {
    override val children: List<HirNode>
        get() = childrenFrom(condition, body)

    override fun prettySelf(): String {
        return "While expr"
    }
}

class HirAssignExpr(val name: String, val rValue: HirExpr, val decl: HirVarDeclaration) : HirExpr() {
    override val children: List<HirNode>
        get() = listOf(rValue)

    override fun prettySelf(): String {
        return "Assign expr: $name"
    }
}

// Expressions

sealed class HirExpr : HirNode()


sealed class HirCallExpr(val name: String, val args: List<HirExpr>) : HirExpr() {
    override val children: List<HirNode>
        get() = args
}

class HirGlobalCallExpr(
        name: String,
        args: List<HirExpr>
) : HirCallExpr(name, args) {
    override fun prettySelf(): String {
        return "Global call: $name"
    }
}

class HirLocalCallExpr(
        name: String,
        args: List<HirExpr>,
        val decl: HirFunctionDeclaration
) : HirCallExpr(name, args) {
    override fun prettySelf(): String {
        return "Local call: $name"
    }
}

class HirIfExpr(
        val condition: HirExpr,
        val thenBranch: HirExpr,
        val elseBranch: HirExpr
) : HirExpr() {
    override val children: List<HirNode>
        get() = childrenFrom(condition, thenBranch, elseBranch)

    override fun prettySelf(): String {
        return "If expr"
    }
}

sealed class HirLiteralExpr : HirExpr()

class HirBoolLiteral(val value: Boolean) : HirLiteralExpr() {
    override val children: List<HirNode>
        get() = emptyList()

    override fun prettySelf(): String {
        return "Bool literal: $value"
    }
}

class HirIntLiteral(val value: Int) : HirLiteralExpr() {
    override val children: List<HirNode>
        get() = emptyList()

    override fun prettySelf(): String {
        return "Int literal: $value"
    }

}

class HirStringLiteral(val value: String) : HirLiteralExpr() {
    override val children: List<HirNode>
        get() = emptyList()

    override fun prettySelf(): String {
        return "String literal: $value"
    }

}

class HirListLiteral(val literals: List<HirLiteralExpr>) : HirLiteralExpr() {
    override val children: List<HirNode>
        get() = literals

    override fun prettySelf(): String {
        return "List literal"
    }

}

class HirIdentifierLiteral(val name: String) : HirLiteralExpr() {
    override val children: List<HirNode>
        get() = emptyList()

    override fun prettySelf(): String {
        return "Identifier literal: $name"
    }

}

class HirFunctionReference(val name: String, val decl: HirFunctionDeclaration) : HirLiteralExpr() {
    override fun prettySelf(): String {
        return "Function reference: $name"
    }

    override val children: List<HirNode>
        get() = emptyList()
}

class HirVarReference(val name: String, val decl: HirVarDeclaration) : HirLiteralExpr() {
    override fun prettySelf(): String {
        return "Var reference: $name"
    }

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
