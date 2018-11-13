package interpreter

import analysis.Keywords
import lexer.Lexer
import lexer.LexerImpl
import lexer.Token
import lexer.TokenType
import parser.*
import java.util.*

sealed class EnvironmentEntry

sealed class FunctionLike : EnvironmentEntry() {
    abstract fun call(args: List<AstNode>, interpreter: Interpreter): AstNode
}

class Macro(val name: String, val parameters: List<String>, val body: List<AstNode>) : FunctionLike() {
    // TODO actually, it is not recursive expansion
    // Actual recursive work should be done in macro expander
    override fun call(args: List<AstNode>, interpreter: Interpreter): AstNode {
        val scope = parameters.zip(args).associateBy({ it.first }) { it.second }
        var body = body
        var transforms = 0
        while (true) {
            val expansionResult = singleExpansion(body, scope, interpreter)
            if (!expansionResult.wasTransformation) return expansionResult.node
            body = expansionResult.newBody
            transforms++
            if (transforms > 1000) throw InterpreterException("Macros is too deep", body.first().textRange)
        }
    }

    private class ExpansionResult(
            val node: AstNode,
            val newBody: List<AstNode>,
            val wasTransformation: Boolean
    )


    private fun singleExpansion(body: List<AstNode>, scope: Map<String, AstNode>, interpreter: Interpreter) : ExpansionResult {
        var wasTransformation = false
        val replacedBody = body.map { bodyNode ->
            transform(bodyNode) { leafNode ->
                if (leafNode.token.type != TokenType.Identifier) {
                    leafNode
                } else {
                    val astNode = scope[leafNode.token.text]
                    if (astNode != null) {
                        wasTransformation = true
                        astNode
                    } else leafNode
                }
            }
        }
        var result: AstNode? = null
        for (node in replacedBody) {
            result = interpreter.eval(node)
        }
        val resultNode = if (result is DataNode) {
            result.node
        } else {
            when (result) {
                null -> emptyListNode()
                else -> interpreter.eval(result)
            }
        }
        return ExpansionResult(resultNode, replacedBody, wasTransformation)
    }
}

sealed class Function : FunctionLike()

class AstFunction(val name: String, val parameters: List<String>, val body: List<AstNode>) : Function() {
    override fun call(args: List<AstNode>, interpreter: Interpreter): AstNode {
        val scope: MutableMap<String, EnvironmentEntry> = parameters.zip(args)
                .associateBy({ it.first }) { Variable(it.second) }.toMutableMap()
        interpreter.env.enterScope(scope)
        val results = mutableListOf<AstNode>()
        for (statement in body) {
            results.add(interpreter.eval(statement))
        }
        interpreter.env.leaveScope(1)
        return results.last()
    }
}

class EnvFunction(val function: (List<AstNode>, Interpreter) -> AstNode) : Function() {
    override fun call(args: List<AstNode>, interpreter: Interpreter): AstNode = function(args, interpreter)
}

class Variable(var value: AstNode) : EnvironmentEntry()

private fun <T> intTFunction(f: (List<Int>) -> T?, tokenTypeMapper: (T) -> TokenType, syntaxKind: SyntaxKind): EnvFunction {
    return EnvFunction { argNodes, interpreter ->
        val values = argNodes.map { (interpreter.eval(it) as LeafNode).token.text.toInt() }
        val tResult = f(values) ?: return@EnvFunction emptyListNode()
        val token = Token(-1, tResult.toString(), tokenTypeMapper(tResult))
        LeafNode(token, syntaxKind)
    }
}

private fun intFunction(f: (List<Int>) -> Int?): EnvFunction {
    return intTFunction(f, { TokenType.Int }, SyntaxKind.IntLiteral)
}

private fun intBoolFunction(f: (List<Int>) -> Boolean): EnvFunction {
    return intTFunction(f, { if (it) TokenType.TrueLiteral else TokenType.FalseLiteral }, SyntaxKind.BoolLiteral)
}


class InterpreterException(reason: String, val range: TextRange) : Exception(reason) {
    override fun toString(): String {
        return "Interpreter $range: $message"
    }
}

class InterpreterEnv(globalScope: MutableMap<String, EnvironmentEntry>) {
    val envStack = ArrayDeque<MutableMap<String, EnvironmentEntry>>()

    init {
        envStack.push(globalScope)
    }

    val globalScope
        get() = envStack.peekFirst()

    fun enterScope(scope: MutableMap<String, EnvironmentEntry>) {
        envStack.push(scope)
    }

    fun leaveScope(count: Int) {
        for (i in 0 until count) {
            envStack.pop()
        }
    }

    fun resolve(name: String): EnvironmentEntry? {
        return envStack.asSequence()
                .map { it[name] }
                .filterNotNull()
                .firstOrNull()
    }

    fun addLocal(name: String, value: EnvironmentEntry) {
        envStack.peekLast()[name] = value
    }
}

val standardEnvFunctions: MutableMap<String, EnvironmentEntry> = mutableMapOf(
        "+" to intFunction { it.sum() },
        "*" to intFunction { it.reduce { acc, i -> acc * i } },
        "-" to intFunction { it[0] - it.drop(1).sum() },
        "/" to intFunction { it[0] / it.drop(1).reduce { acc, i -> acc * i } },
        ">" to intBoolFunction { list -> list.drop(1).all { it < list[0] } },
        "<" to intBoolFunction { list -> list.drop(1).all { it > list[0] } },
        "=" to intBoolFunction { list -> list.drop(1).all { it == list[0] } },
        "print" to intBoolFunction { println(it);true }
)


class Interpreter(internal val env: InterpreterEnv = InterpreterEnv(standardEnvFunctions)) {

    // assumes no macro inside
    @Throws(InterpreterException::class)
    fun eval(node: AstNode): AstNode {
        return when (node) {
            is DataNode -> node
            is LeafNode -> {
                if (node.token.type == TokenType.Identifier) {
                    val ident = node.token.text
                    val entry = env.resolve(ident) ?: err("No $ident found in env", node)
                    when (entry) {
                        is Variable -> eval(entry.value)
                        else -> error("Expected variable")
                    }
                } else {
                    node
                }
            }
            is ListNode -> return evalList(node)
            is FileNode -> {
                if (node.children.isEmpty()) return emptyListNode()
                val results = mutableListOf<AstNode>()
                for (child in node.children) {
                    results.add(eval(child))
                }
                results.last()
            }
        }
    }

    private fun evalList(node: AstNode): AstNode {
        val children = node.children
        if (children.isEmpty()) {
            return node
        }
        val first = children.first() as? LeafNode
                ?: err("First list node must be leaf", node)
        if (first.token.type != TokenType.Identifier) {
            err("First node of list must be identifier", first)
        }
        val firstNodeText = first.token.text
        val childrenCount = children.size
        return when (firstNodeText) {
            Keywords.IF_KW -> evalIf(childrenCount, node, children)
            Keywords.WHILE_KW -> evalWhile(childrenCount, node, children)
            Keywords.DEFN_KW -> evalDefn(childrenCount, node, children)
            Keywords.LET_KW -> evalLet(childrenCount, node, children)
            Keywords.SET_KW -> evalSet(childrenCount, node, children)
            Keywords.MACRO_KW -> evalMacro(childrenCount, node, children)
            // TODO lambda?
            else -> {
                val entry = env.resolve(firstNodeText) ?: err("No definition of $firstNodeText in env", node)
                when (entry) {
                    is FunctionLike -> {
                        //                                if (entry.parameters.size != childrenCount - 1) err("Wrong count of args for call, ${entry.parameters.size} expected", node)
                        entry.call(children.drop(1), this)
                    }
                    is Variable -> {
                        val fn = asFunction(entry.value) ?: err("Variable $firstNodeText refers not to function", node)
                        //                                if (fn.parameters.size != childrenCount - 1) err("Wrong count of args for call, ${fn.parameters.size} expected", node)
                        fn.call(children.drop(1), this)
                    }
                }
            }
        }
    }

    private fun evalSet(childrenCount: Int, node: AstNode, children: List<AstNode>): ListNode {
        if (childrenCount != 3) err("Set construction must have 3 children", node)
        val nameNode = children[1]
        nameNode as? LeafNode ?: err("Assignment node must be ident", nameNode)
        val varName = nameNode.token.text
        val variable = env.resolve(varName) as? Variable ?: err("Variable ${nameNode.lispy()} not found", nameNode)
        variable.value = eval(children[2])
        return emptyListNode()
    }

    private fun evalMacro(childrenCount: Int, node: AstNode, children: List<AstNode>): ListNode {
        val macro = extractMacro(childrenCount, node, children)
        env.addLocal(macro.name, macro)
        return emptyListNode()
    }

    private fun evalLet(childrenCount: Int, node: AstNode, children: List<AstNode>): AstNode {
        if (childrenCount < 3) err("Let construction must have 3 or more children", node)
        val declNode = children[1]
        val declarations = declNode as? ListNode ?: err("Declarations node in let must be list", declNode)
        for (declaration in declarations.children) {
            declaration as? ListNode ?: err("Declaration node must be list", declaration)
            if (declaration.children.size != 2) err("Declaration node must have 2 children", declaration)
            val declIdentNode = declaration.children[0]
            val firstDeclNode = declIdentNode as? LeafNode
                    ?: err("First declaration node must be identifier", declIdentNode)
            if (firstDeclNode.token.type != TokenType.Identifier) err("First declaration node must be identifier", declIdentNode)
            val varText = firstDeclNode.token.text
            val initValue = declaration.children[1]
            env.enterScope(hashMapOf(varText to Variable(initValue)))
        }
        val results = mutableListOf<AstNode>()
        for (i in 2 until children.size) {
            results.add(eval(children[i]))
        }
        env.leaveScope(declarations.children.size)
        return results.last()
    }

    private fun evalDefn(childrenCount: Int, node: AstNode, children: List<AstNode>): AstNode {
        // (defn foo (x y z) () ())
        val function = extractFunction(childrenCount, node, children)
        env.addLocal(function.name, function)
        return node
    }

    private fun evalWhile(childrenCount: Int, node: AstNode, children: List<AstNode>): ListNode {
        if (childrenCount < 3) err("While must have at least 3 children", node)
        val condition = children[1]
        while (eval(condition).asBool() == true) {
            for (i in (2 until childrenCount)) {
                val child = children[i]
                eval(child)
            }
        }
        return emptyListNode()
    }

    private fun evalIf(childrenCount: Int, node: AstNode, children: List<AstNode>): AstNode {
        if (childrenCount != 3 && childrenCount != 4) err("If must have 3 or 4 children", node)
        val conditionNode = children[1]
        val condition = eval(conditionNode).asBool() ?: err("Condition must evaluate to boolean", node)
        return when {
            condition -> eval(children[2])
            else -> when (childrenCount) {
                3 -> emptyListNode() // no else
                else -> eval(children[3])
            }
        }
    }

    private fun <T> extractFunctionLike(
            childrenCount: Int,
            node: AstNode,
            children: List<AstNode>,
            constructor: (name: String, node: AstNode, parameters: List<String>, body: List<AstNode>, interpreter: Interpreter) -> T
    ): T {
        if (childrenCount < 3) err("Functional node must have at least 4 children", node)
        val nameNode = children[1]
        nameNode as? LeafNode ?: err("Name node must be leaf", nameNode)
        if (nameNode.token.type != TokenType.Identifier) err("Name node must be identifier", nameNode)
        val parameterNode = children[2]
        parameterNode as? ListNode ?: err("Parameters node must be list", parameterNode)
        val parameterNodes = parameterNode.children
        val parameters = mutableListOf<String>()
        for (parameter in parameterNodes) {
            parameter as? LeafNode ?: err("Parameter node must be leaf", parameter)
            parameters.add(parameter.token.text)
        }
        val body = children.drop(3)
        return constructor(nameNode.token.text, node, parameters, body, this)
    }

    private fun extractFunction(childrenCount: Int, node: AstNode, children: List<AstNode>): AstFunction {
        return extractFunctionLike(childrenCount, node, children) {
            name, _, parameters, body, interpreter ->
            AstFunction(name, parameters, body)
        }
    }

    private fun extractMacro(childrenCount: Int, node: AstNode, children: List<AstNode>): Macro {
        return extractFunctionLike(childrenCount, node, children) {
            name, _, parameters, body, interpreter ->
            Macro(name, parameters, body)
        }
    }

    private fun asFunction(node: AstNode): Function? {
        val children = node.children
        if (children.isEmpty()) return null
        val first = children[0] as? LeafNode ?: return null
        if (first.token.type != TokenType.Identifier) return null
        val name = first.token.text
        return try {
            extractFunction(children.size, node, children)
        } catch (e: InterpreterException) {
            null
        }
    }


    private fun err(reason: String, node: AstNode): Nothing = throw InterpreterException(reason, node.textRange)

    private fun AstNode.asBool(): Boolean? {
        return when {
            this is LeafNode -> when (token.type) {
                TokenType.TrueLiteral -> true
                TokenType.FalseLiteral -> false
                else -> null
            }
            else -> null
        }
    }
}

private fun emptyListNode() = ListNode(emptyList(), TextRange(0, 0))


// Required for macro substitution
fun transform(node: AstNode, action: (LeafNode) -> AstNode): AstNode {
    val children = node.children
    return when (node) {
        is LeafNode -> action(node)
        is ListNode -> ListNode(children.map { transform(it, action) }, node.textRange)
        is FileNode -> FileNode(children.map { transform(it, action) }, node.textRange)
        is DataNode -> DataNode(transform(node.node, action), node.textRange)
    }
}

fun main(args: Array<String>) {
    val lexerImpl: Lexer = LexerImpl()
    val parser = Parser()
//    lexer.tokenize()
//    val parse = parser.parse(lexerImpl.tokenize("(if #t (if #f 12 22) 44)"))
//    val parse = parser.parse(lexerImpl.tokenize("(let ((x 44) (y (defn foo (x) (+ x 100)))) (set x 77) (y 12))"))
    val parse = parser.parse(lexerImpl.tokenize("""
        (defn sqr (x) (* x x))
        (macro stat-sqr (x) `(* x x))
        (let ((i 0))
            (while (< i 10)
                (print (sqr i))
                (set i (+ i 1))
            )
            (stat-sqr 12)
        )
    """.trimIndent()))
    if (parse is ParseResult.Error) {
        println(parse)
        return
    }
    println(Interpreter().eval((parse as ParseResult.Ok).node).lispy())
}