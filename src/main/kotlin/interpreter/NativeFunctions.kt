package interpreter

import lexer.Token
import lexer.TokenType
import parser.*

// In this functions text range is not preserved!

private fun Boolean.toAst(): LeafNode {
    val token = when (this) {
        true -> Token(0, "#t", TokenType.TrueLiteral)
        false -> Token(0, "#f", TokenType.FalseLiteral)
    }
    return LeafNode(token, SyntaxKind.BoolLiteral)
}

private fun Int.toAst() : LeafNode {
    return LeafNode(Token(0, this.toString(), TokenType.Int), SyntaxKind.IntLiteral)
}

private fun typeAssert(value: AstNode, kind: SyntaxKind) {
    if (value.syntaxKind != kind) {
        err("${value.lispy()} expected to be of type $kind, but was: ${value.syntaxKind}", value)
    }
}

private fun AstNode.asInt(): Int {
    typeAssert(this, SyntaxKind.IntLiteral)
    return (this as LeafNode).token.text.toInt()
}


private fun AstNode.asBool(): Boolean {
    typeAssert(this, SyntaxKind.BoolLiteral)
    return when (toString()) {
        "#t" -> true
        "#f" -> false
        else -> err("Bad value of int literal", this)
    }
}

class BinIntNativeFun(f: (Int, Int) -> Int) : NativeFunction ({ args, _ ->
    val left = args[0].asInt()
    val right = args[1].asInt()
    f(left, right).toAst()
})

class BinBoolNativeFun(f: (Int, Int) -> Boolean) : NativeFunction ({ args, _ ->
    val left = args[0].asInt()
    val right = args[1].asInt()
    f(left, right).toAst()
})

object NativeEqFun : NativeFunction({ args, _ ->
    val left = args[0]
    val right = args[1]
    when {
        left.syntaxKind == SyntaxKind.BoolLiteral && right.syntaxKind == SyntaxKind.BoolLiteral ->
            (left.asBool() == right.asBool()).toAst()
        left.syntaxKind == SyntaxKind.IntLiteral && right.syntaxKind == SyntaxKind.IntLiteral ->
            (left.asInt() == right.asInt()).toAst()
        else -> err("Unexpected types of operands in eq operator", left)
    }
})

object ConsFun : NativeFunction({args, _ ->
    val list = args[0]
    val element = args[1]
    ListNode(list.children + element, list.textRange)
})

object TailFun : NativeFunction({args, _ ->
    val list = args[0]
    typeAssert(list, SyntaxKind.Data)
    val tail = (list as DataNode).node.children.drop(1)
    DataNode(ListNode(tail, list.textRange), list.textRange)
})

object FirstFun : NativeFunction({args, _ ->
    val list = args[0]
    typeAssert(list, SyntaxKind.Data)
    (list as DataNode).node.children.first()
})

object ListSizeFun : NativeFunction({args, _ ->
    val list = args[0]
    val size = when (list) {
        is ListNode -> {
            if (list.children.isNotEmpty()) err("Unexpected non empty non data node in size function", list)
            0
        }
        is DataNode -> list.node.children.size
        else -> err("Unexpected node type for size function", list)
    }
    size.toAst()
})

object PrintErrorAndExitFun : NativeFunction({args, _ ->
    val text = args[0]
    err((text as LeafNode).token.text, text)
})

object PrintFun : NativeFunction({args, _ ->
    val text = args[0]
    println(text.lispy())
    emptyListNode()
})

internal val binAdd = BinIntNativeFun { left, right ->
    left + right
}
internal val binSub = BinIntNativeFun { left, right -> left - right }
internal val binMul = BinIntNativeFun { left, right -> left * right }
internal val binDiv = BinIntNativeFun { left, right -> left / right }
internal val binRem = BinIntNativeFun { left, right -> left % right }
internal val binGt = BinBoolNativeFun { left, right ->
    left > right
}

val standardNativeFunctions = mapOf(
        "r__add" to binAdd,
        "r__sub" to binSub,
        "r__mul" to binMul,
        "r__div" to binDiv,
        "r__rem" to binRem,
        "r__gt" to binGt,
        "r__eq" to NativeEqFun,
        "r__withElement" to ConsFun,
        "r__tail" to TailFun,
        "r__first" to FirstFun,
        "r__size" to ListSizeFun,
        "r__printErrorAndExit" to PrintErrorAndExitFun,
        "r__print" to PrintFun
)