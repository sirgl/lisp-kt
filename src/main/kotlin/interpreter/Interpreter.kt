package interpreter

import analysis.Matchers
import lexer.Token
import lexer.TokenType
import parser.*
import java.util.*

class NativeFunction(val function: (List<AstNode>, Interpreter) -> AstNode) {
    fun call(args: List<AstNode>, interpreter: Interpreter): AstNode = function(args, interpreter)
}

private fun <T> intTFunction(f: (List<Int>) -> T?, tokenTypeMapper: (T) -> TokenType, syntaxKind: SyntaxKind): NativeFunction {
    return NativeFunction { argNodes, interpreter ->
        val values = argNodes.map { (interpreter.eval(it) as LeafNode).token.text.toInt() }
        val tResult = f(values) ?: return@NativeFunction emptyListNode()
        val token = Token(-1, tResult.toString(), tokenTypeMapper(tResult))
        LeafNode(token, syntaxKind)
    }
}

private fun intFunction(f: (List<Int>) -> Int?): NativeFunction {
    return intTFunction(f, { TokenType.Int }, SyntaxKind.IntLiteral)
}

private fun intBoolFunction(f: (List<Int>) -> Boolean): NativeFunction {
    return intTFunction(f, { if (it) TokenType.TrueLiteral else TokenType.FalseLiteral }, SyntaxKind.BoolLiteral)
}


class InterpreterException(reason: String, val range: TextRange) : Exception(reason) {
    override fun toString(): String {
        return "Interpreter $range: $message"
    }
}

class InterpreterEnv(
        val globalScope: MutableMap<String, AstNode> = mutableMapOf(),
        private val nativeFunctions: Map<String, NativeFunction> = standardEnvFunctions
) {
    private val envStack = ArrayDeque<MutableMap<String, AstNode>>()

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

    fun findNativeFun(runtimeName: String): NativeFunction? {
        return nativeFunctions[runtimeName]
    }

    fun resolve(name: String): AstNode? {
        return envStack.asSequence()
                .map { it[name] }
                .filterNotNull()
                .firstOrNull()
    }

    /**
     * @return true, if successfully found variable
     */
    fun assign(name: String, value: AstNode) : Boolean {
        for (space in envStack) {
            if (name in space) {
                space[name] = value
                return true
            }
        }
        return false
    }

    fun addToScope(name: String, value: AstNode) {
        envStack.peekFirst()[name] = value
    }
}

val standardEnvFunctions: MutableMap<String, NativeFunction> = mutableMapOf(
        "r__add" to intFunction { it.sum() },
        "r__mul" to intFunction { it.reduce { acc, i -> acc * i } },
        "r__sub" to intFunction { it[0] - it.drop(1).sum() },
        "r__div" to intFunction { it[0] / it.drop(1).reduce { acc, i -> acc * i } },
        "r__gt" to intBoolFunction { list -> list.drop(1).all { it < list[0] } },
        "r__lt" to intBoolFunction { list -> list.drop(1).all { it > list[0] } },
        "r__eq" to intBoolFunction { list -> list.drop(1).all { it == list[0] } },
        "__print" to intBoolFunction { println(it);true }
)


class Interpreter(private val env: InterpreterEnv = InterpreterEnv(mutableMapOf(), standardEnvFunctions)) {

    // assumes no macro inside
    @Throws(InterpreterException::class)
    fun eval(node: AstNode): AstNode {
        return when (node) {
            is DataNode -> node
            is LeafNode -> {
                if (node.token.type == TokenType.Identifier) {
                    val ident = node.token.text
                    env.resolve(ident) ?: err("No $ident found in env", node)
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
            Matchers.DEFNAT.matches(node) -> {
                val nativeFunction = Matchers.DEFNAT.forceExtract(node)
                env.addToScope(nativeFunction.nameInProgram, node)
                node
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
                env.addToScope(defnInfo.name, node)
                node
            }
            Matchers.MACRO.matches(node) -> {
                val macroInfo = Matchers.MACRO.forceExtract(node)
                env.addToScope(macroInfo.name, node)
                emptyListNode()
            }
            Matchers.SET.matches(node) -> {
                val setInfo = Matchers.SET.forceExtract(node)
                if (!env.assign(setInfo.name, eval(setInfo.newValue))) {
                    err("Variable ${setInfo.name} not found", node)
                }
                emptyListNode()
            }
            Matchers.LET.matches(node) -> {
                val letInfo = Matchers.LET.forceExtract(node)
                for (declaration in letInfo.declarations) {
                    env.enterScope()
                    env.addToScope(declaration.name, eval(declaration.initializer))
                }
                var last: AstNode? = null
                for (bodyNode in letInfo.body) {
                    last = eval(bodyNode)
                }
                env.leaveScope(letInfo.declarations.size)
                return last!! // matcher guarantees, that body is not empty
            }
            else -> {
                // it is either function call or macro call
                val entry = env.resolve(firstNodeText) ?: err("No definition of $firstNodeText in env", node)
                val args = children.drop(1)
                return when {
                    Matchers.DEFN.matches(entry) -> callDefn(entry, args)
                    Matchers.DEFNAT.matches(entry) -> callNative(entry, args)
                    Matchers.MACRO.matches(entry) -> callMacro(entry, args)
                    else -> throw UnsupportedOperationException()
                }
            }
        }
    }

    private fun callMacro(entry: AstNode, args: List<AstNode>): AstNode {
        val macroInfo = Matchers.MACRO.forceExtract(entry)
        // TODO support varargs
        val replacementMap = macroInfo.parameters.map { it.name }.zip(args)
            .associateBy({ it.first }) { it.second }
        //        var wasTransformation = false
        val replacedBody = macroInfo.body.map { bodyNode ->
            transform(bodyNode) { leafNode ->
                if (leafNode.token.type != TokenType.Identifier) {
                    leafNode
                } else {
                    replacementMap[leafNode.token.text] ?: leafNode
                }
            }
        }
        var result: AstNode? = null
        for (node in replacedBody) {
            result = eval(node)
        }
        return if (result is DataNode) {
            result.node
        } else {
            when (result) {
                null -> emptyListNode()
                else -> eval(result)
            }
        }
    }

    private fun callNative(
        entry: AstNode,
        args: List<AstNode>
    ): AstNode {
        val nativeFun = Matchers.DEFNAT.forceExtract(entry)
        val nameInProgram = nativeFun.nameInProgram
        if (args.size != nativeFun.parameters.size) {
            err("Parameter count and args count must match ($nameInProgram)", entry)
        }
        val nameInRuntime = nativeFun.nameInRuntime
        val nativeFunction = env.findNativeFun(nameInRuntime)
            ?: err("No $nameInProgram runtime function registered ($nameInRuntime runtime name expected)", entry)
        return nativeFunction.call(args, this)
    }

    private fun callDefn(
        entry: AstNode,
        args: List<AstNode>
    ): AstNode {
        val defnNode = Matchers.DEFN.forceExtract(entry)
        if (args.size != defnNode.parameters.size) {
            err("Parameter count and args count must match (${defnNode.name})", entry)
        }
        env.enterScope()
        for ((index, parameter) in defnNode.parameters.map { it.name }.withIndex()) {
            // TODO support varargs
            env.addToScope(parameter, args[index])
        }
        var last: AstNode? = null
        for (bodyNode in defnNode.body) {
            last = eval(bodyNode)
        }
        env.leaveScope(defnNode.parameters.size)
        return last!!
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