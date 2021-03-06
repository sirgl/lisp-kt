package hir

import analysis.Matchers
import deps.RealDependencyEntry
import deps.dfs
import lexer.TokenType
import linting.AppendingSink
import linting.Lint
import linting.Severity
import linting.Subsystem
import parser.*
import util.ResultWithLints
import util.Source
import java.util.*
import kotlin.collections.HashMap

class HirLowering(private val implicitImports: List<HirImport>) {
    fun lower(target: RealDependencyEntry): ResultWithLints<List<HirFile>> {
        val context = LoweringContext()
        val lints = mutableListOf<Lint>()
        val files = mutableListOf<HirFile>()
        var isError = false
        target.dfs {
            it as RealDependencyEntry
            val ast = it.ast
            val file = lower(ast.root, ast.source, context, it === target)
            lints.addAll(file.lints)
            if (file.isError()) {
                isError = true
                return@dfs
            }
            files.add(file.drainTo(lints)!!)
        }
        if (isError) {
            return ResultWithLints.Error(lints)
        }
        return ResultWithLints.Ok(files)
    }

    /**
     * It is important to lower in dfs post order to make sure all dependencies get into context before lowering unit
     */
    private fun lower(root: FileNode, source: Source, context: LoweringContext, isTarget: Boolean): ResultWithLints<HirFile> {
        return UnitHirLowering(root, source, context, implicitImports, isTarget).lower()
    }

}


private class LoweringContext {
    val resolveStack: Deque<MutableMap<String, HirDeclaration>> = ArrayDeque()

    init {
        enterScope()
    }

    fun enterScope() {
        resolveStack.push(HashMap())
    }

    fun leaveScope(count: Int = 1) {
        for (i in 0 until count) {
            resolveStack.pop()
        }
    }

    fun addToScope(declaration: HirDeclaration) {
        resolveStack.last[declaration.name] = declaration
    }

    fun resolve(name: String): HirDeclaration? {
        return resolveStack
                .asSequence()
                .map { scopeDeclarations -> scopeDeclarations[name] }
                .firstOrNull { it != null }
    }

    /**
     * @param f inside all [declarations] will be available
     */
    inline fun <T> withDeclarations(declarations: List<HirDeclaration>, f: () -> T): T {
        enterScope()
        for (decl in declarations) {
            addToScope(decl)
        }
        val result = f()
        leaveScope()
        return result
    }
}

private class UnitHirLowering(
        val root: AstNode,
        val source: Source,
        val context: LoweringContext,
        implicitImports: List<HirImport>,
        val isTarget: Boolean
) {
    val imports = implicitImports.toMutableList()
    val lints = mutableListOf<Lint>()
    val functions = mutableListOf<HirFunctionDeclaration>()
    val macroasms = mutableListOf<HirMacroasmDefinition>()
    var moduleName: String? = null
    val sink = AppendingSink(lints)

    fun wasError(): Boolean {
        return lints.any { it.severity == Severity.Error }
    }


    fun lower(): ResultWithLints<HirFile> {
        val block = lowerBlock(root.children, true) ?: return ResultWithLints.Error(lints)
        // Convention for top level code in file (all code in HIR must be in function, so synthetic one is created)
        val name = source.path.replace('/', '_').replace('.', '_') + "__init"
        functions.add(HirFunctionDefinition(name, emptyList(), block, true, isTarget))
        return ResultWithLints.Ok(HirFile(source, imports, functions, macroasms, moduleName))
    }

    fun lowerBlock(nodes: List<AstNode>, isTopLevel: Boolean = false): HirBlockExpr? {
        if (nodes.isEmpty()) {
            return emptyBlock()
        }
        val stmts = mutableListOf<HirStmt>()
        for ((index, child) in nodes.withIndex()) {
            val expr = lowerExpr(child, isTopLevel, index == 0)
            if (wasError()) return null
            if (expr != null) {
                stmts.add(HirExprStmt(expr))
            }
        }
        if (stmts.isEmpty()) {
            return emptyBlock()
        }
        val last = stmts.last() as? HirExprStmt
                ?: return if (isTopLevel) {
                    emptyBlock()
                } else {
                    errorLint("Last node in block expected to be expr", nodes.last().textRange)
                    null
                }
        val expr = last.expr
        return HirBlockExpr(stmts.subList(0, stmts.size - 1), expr)
    }

    fun lowerExpr(node: AstNode, isTopLevel: Boolean = false, isFirst: Boolean = true): HirExpr? {
        return when (node) {
            is LeafNode -> {
                val type = node.token.type
                if (type == TokenType.Identifier) {
                    val name = node.token.text
                    val decl = context.resolve(name) ?: return null
                    when (decl) {
                        is HirVarDeclaration -> HirVarReference(name, decl)
                        is HirFunctionDefinition -> HirFunctionReference(name, decl)
                        else -> {
                            errorLint("Unresolved reference", node.textRange)
                            return null
                        }
                    }
                } else {
                    return lowerLiteral(node)
                }
            }
            is DataNode -> lowerLiteral(node)
            is FileNode -> throw IllegalStateException()
            is ListNode -> {
                if (node.children.isEmpty()) {
                    emptyListLiteral()
                } else when {
                    Matchers.MODULE.matches(node, source, sink) -> {
                        if (!isTopLevel) {
                            errorLint("Module declaration are allowed only on top level", node.textRange)
                            null
                        } else if (!isFirst) {
                            errorLint("Module declaration are possible only as first child of file", node.textRange)
                            null
                        } else {
                            val moduleInfo = Matchers.MODULE.extract(node, source).drainTo(lints) ?: return null
                            moduleName = moduleInfo.name
                            null
                        }
                    }
                    Matchers.IMPORT.matches(node, source, sink) -> {
                        if (!isTopLevel) {
                            errorLint("Imports are allowed only on top level", node.textRange)
                            return null
                        }
                        val importInfo = Matchers.IMPORT.extract(node, source).drainTo(lints) ?: return null
                        imports.add(HirImport(importInfo.name, true))
                        null
                    }
                    Matchers.DEFN.matches(node, source, sink) -> {
                        val defnInfo = Matchers.DEFN.extract(node, source).drainTo(lints) ?: return null
                        val params = defnInfo.parameters.map { HirParameter(it.name, it.isVararg) }
                        val bodyBlock = context.withDeclarations(params) {
                            lowerBlock(defnInfo.body) ?: return null
                        }
                        val declaration = HirFunctionDefinition(defnInfo.name, params, bodyBlock)
                        functions.add(declaration)
                        context.addToScope(declaration)
                        HirFunctionReference(defnInfo.name, declaration)
                    }
                    Matchers.LET.matches(node, source, sink) -> {
                        val letInfo = Matchers.LET.extract(node, source).drainTo(lints) ?: return null
                        val vars = mutableListOf<HirVarDeclStmt>()
                        for (declaration in letInfo.declarations) {
                            val varDeclStmt =
                                    HirVarDeclStmt(declaration.name, lowerExpr(declaration.initializer) ?: return null)
                            vars.add(varDeclStmt)
                            context.enterScope()
                            context.addToScope(varDeclStmt)
                        }
                        val letBlock = lowerBlock(letInfo.body) ?: return null
                        context.leaveScope(letInfo.declarations.size)
                        val blockWithDeclarations = HirBlockExpr(vars, letBlock)
                        blockWithDeclarations
                    }
                    Matchers.IF.matches(node, source, sink) -> {
                        val ifInfo = Matchers.IF.extract(node, source).drainTo(lints) ?: return null
                        val condition = lowerExpr(ifInfo.condition) ?: return null
                        val thenBranch = lowerExpr(ifInfo.thenBranch) ?: return null
                        val elseNode = ifInfo.elseBranch
                        val elseBranch = if (elseNode == null) {
                            emptyListLiteral()
                        } else {
                            lowerExpr(elseNode)
                        } ?: return null
                        HirIfExpr(condition, thenBranch, elseBranch)
                    }
                    Matchers.WHILE.matches(node, source, sink) -> {
                        val whileInfo = Matchers.WHILE.extract(node, source).drainTo(lints) ?: return null
                        val condition = lowerExpr(whileInfo.condition) ?: return null
                        val block = lowerBlock(whileInfo.body) ?: return null
                        HirWhileExpr(condition, block)
                    }
                    Matchers.SET.matches(node, source, sink) -> {
                        val setInfo = Matchers.SET.extract(node, source).drainTo(lints) ?: return null
                        val newValue = lowerExpr(setInfo.newValue) ?: return null
                        val name = setInfo.name
                        val hirDeclaration = context.resolve(name)
                        if (hirDeclaration is HirParameter) {
                            errorLint("Parameters can't be changed: $name", node.textRange)
                            return null
                        }
                        val varDeclaration = hirDeclaration as? HirVarDeclaration
                        if (varDeclaration == null) {
                            errorLint("Unresolved variable reference: $name", node.textRange)
                            return null
                        }
                        HirAssignExpr(name, newValue, varDeclaration)
                    }
                    Matchers.MACROASM_EXPANDED.matches(node, source, sink) -> {
                        val macroInfo = Matchers.MACROASM_EXPANDED.extract(node, source).drainTo(lints) ?: return null
                        val declaration = HirMacroasmDefinition(macroInfo.name, macroInfo.asmText)
                        context.addToScope(declaration)
                        macroasms.add(declaration)
                        emptyListLiteral()
                    }
                    Matchers.DEFNAT.matches(node, source, sink) -> {
                        val nativeFun = Matchers.DEFNAT.extract(node, source).drainTo(lints)
                                ?: return null
                        val declaration = HirNativeFunctionDeclaration(
                                nativeFun.nameInProgram,
                                nativeFun.nameInRuntime,
                                nativeFun.parameters.map { HirParameter(it.name, it.isVararg) }
                        )
                        context.addToScope(declaration)
                        functions.add(declaration)
                        HirFunctionReference(nativeFun.nameInProgram, declaration)
                    }
                    else -> {
                        val children = node.children
                        if (children.isEmpty()) return null
                        val first = children[0]
                        if (first !is LeafNode || first.token.type != TokenType.Identifier) {
                            errorLint("First node of list must be keyword or identifier", first.textRange)
                            return null
                        }
                        val name = first.token.text
                        val argsNodes = children.drop(1)
                        val args = mutableListOf<HirExpr>()
                        for (argsNode in argsNodes) {
                            args.add(lowerExpr(argsNode, false, false) ?: return null)
                        }
                        val declaration = context.resolve(name)
                        when (declaration) {
                            is HirFunctionDeclaration -> lowerFuncDeclarationCall(args, declaration, first)
                            is HirVarDeclaration -> HirCallByReferenceExpr(HirVarReference(name, declaration), args)
                            else -> {
                                errorLint("Unresolved function reference: $name", first.textRange)
                                return null
                            }
                        }
                    }
                }
            }
        }

    }

    private fun lowerFuncDeclarationCall(args: List<HirExpr>, declaration: HirFunctionDeclaration,
                                         first: AstNode): HirLocalCallExpr? {
        if (args.size != declaration.parameters.size) {
            if (args.size <= declaration.parameters.size || !declaration.hasVararg()) {
                errorLint("Parameter count and args count must match: ${declaration.name}", first.textRange)
                return null
            }
        }
        return HirLocalCallExpr(declaration.name, args, declaration)
    }


    private fun lowerLiteral(node: AstNode): HirLiteralExpr {
        return when (node) {
            is LeafNode -> {
                val type = node.token.type
                val text = node.token.text
                when (type) {
                    TokenType.Identifier -> HirIdentifierLiteral(text)
                    TokenType.TrueLiteral -> HirBoolLiteral(true)
                    TokenType.FalseLiteral -> HirBoolLiteral(false)
                    TokenType.Int -> HirIntLiteral(text.toInt())
                    TokenType.String -> HirStringLiteral(text.substring(1, text.lastIndex))
                    else -> throw IllegalStateException()
                }
            }
            is DataNode -> HirListLiteral(node.node.children.map { lowerLiteral(it) })
            // ListNode is possible as literal only inside DataNode
            is ListNode -> HirListLiteral(node.children.map { lowerLiteral(it) })
            is FileNode -> throw IllegalStateException()
        }
    }

    private fun emptyBlock() = HirBlockExpr(emptyList(), emptyListLiteral())

    private fun emptyListLiteral() = HirListLiteral(emptyList())


    private fun errorLint(text: String, textRange: TextRange) {
        lints.add(Lint(text, textRange, Severity.Error, Subsystem.LoweringToHir, source))
    }
}