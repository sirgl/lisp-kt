package interpreter

import analysis.Matchers
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
        val env = interpreter.env
        env.enterScope()
        for ((index, parameter) in parameters.withIndex()) {
            val arg = args[index]
            env.addToScope(parameter, Variable(arg))
        }

        val results = mutableListOf<AstNode>()
        for (statement in body) {
            results.add(interpreter.eval(statement))
        }
        env.leaveScope(1)
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

    fun enterScope() {
        envStack.push(mutableMapOf())
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

    fun addToScope(name: String, value: EnvironmentEntry) {
        envStack.peekFirst()[name] = value
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
        return when {
            Matchers.NATIVE_FUNCTION.matches(node) -> {
                // TODO place here just reference to real native function
                emptyListNode()
            }
            Matchers.IF.matches(node) -> {
                val ifInfo = Matchers.IF.forceExtract(node)
                if (eval(ifInfo.condition).asBool() ?: err("Condition must evaluate to boolean", node)) {
                    eval(ifInfo.thenBranch)
                } else {
                    eval(ifInfo.elseBranch ?: emptyListNode())
                }
            }
            Matchers.WHILE.matches(node) -> {
                val whileInfo = Matchers.WHILE.forceExtract(node)
                while (eval(whileInfo.condition).asBool() ?: err("Condition must evaluate to boolean", node)) {
                    for (bodyNode in whileInfo.body) {
                        eval(bodyNode)
                    }
                }
                emptyListNode()
            }
            Matchers.DEFN.matches(node) -> {
                val defnInfo = Matchers.DEFN.forceExtract(node)
                val function = AstFunction(defnInfo.name, defnInfo.parameters, defnInfo.body)
                env.addToScope(defnInfo.name, function)
                node
            }
            Matchers.MACRO.matches(node) -> {
                val macroInfo = Matchers.MACRO.forceExtract(node)
                val macro = Macro(macroInfo.name, macroInfo.parameters, macroInfo.body)
                env.addToScope(macroInfo.name, macro)
                emptyListNode()
            }
            Matchers.SET.matches(node) -> {
                val setInfo = Matchers.SET.forceExtract(node)
                val variable = env.resolve(setInfo.name) as? Variable
                        ?: err("Variable ${setInfo.name} not found", node)
                variable.value = eval(setInfo.newValue)
                emptyListNode()
            }
            Matchers.LET.matches(node) -> {
                val letInfo = Matchers.LET.forceExtract(node)
                for (declaration in letInfo.declarations) {
                    env.enterScope()
                    env.addToScope(declaration.name, Variable(eval(declaration.initializer)))
                }
                var last: AstNode? = null
                for (bodyNode in letInfo.body) {
                    last = eval(bodyNode)
                }
                env.leaveScope(letInfo.declarations.size)
                return last!! // matcher guarantees, that body is not empty
            }
            else -> {
                val entry = env.resolve(firstNodeText) ?: err("No definition of $firstNodeText in env", node)
                when (entry) {
                    is FunctionLike -> {
                        // TODO parameter count validation
                        //                                if (entry.parameters.size != childrenCount - 1) err("Wrong count of args for call, ${entry.parameters.size} expected", node)
                        entry.call(children.drop(1), this)
                    }
                    is Variable -> {
                        val defnInfo = Matchers.DEFN.forceExtract(entry.value)
                        val function = AstFunction(defnInfo.name, defnInfo.parameters, defnInfo.body)
                        function.call(children.drop(1), this)
                    }
                }
            }
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