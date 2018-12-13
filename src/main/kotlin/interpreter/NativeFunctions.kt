package interpreter

import lexer.Token
import lexer.TokenType
import parser.AstNode
import parser.LeafNode
import parser.SyntaxKind

internal val printFun = NativeFunction { args, _ ->
    println(args.first().lispy())
    emptyListNode()
}

private fun binaryInt(left: Int, right: Int, f: (Int, Int) -> Int) : LeafNode {
    return LeafNode(Token(0, f(left, right).toString(), TokenType.Int), SyntaxKind.IntLiteral)
}

private fun binaryBool(left: Int, right: Int, f: (Int, Int) -> Int) : LeafNode {
    return LeafNode(Token(0, f(left, right).toString(), TokenType.Int), SyntaxKind.IntLiteral)
}

private fun typeAssert(value: AstNode, kind: SyntaxKind) {
    if (value.syntaxKind != kind) {
        err("Expected to be of type $kind", value)
    }
}

private fun AstNode.asInt(): Int {
    typeAssert(this, SyntaxKind.IntLiteral)
    return (this as LeafNode).token.text.toInt()
}

class BinIntNativeFun(f: (Int, Int) -> Int) : NativeFunction ({ args, _ ->
    val left = args[0].asInt()
    val right = args[1].asInt()
    binaryInt(left, right, f)
})

//class BinBoolNativeFun(f: (Int, Int) -> Boolean) : NativeFunction ({ args, _ ->
//    val left = args[0].asInt()
//    val right = args[1].asInt()
//    binaryInt(left, right, f)
//})

internal val binAdd = BinIntNativeFun { left, right -> left + right }
internal val binSub = BinIntNativeFun { left, right -> left - right }
internal val binMul = BinIntNativeFun { left, right -> left * right }
internal val binDiv = BinIntNativeFun { left, right -> left / right }
internal val binRem = BinIntNativeFun { left, right -> left % right }