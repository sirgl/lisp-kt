package tools.codegen

interface KtTempl

interface KtTemplIndentable {
    fun str(indent: Int): String
}


sealed class KtExprTempl : KtTempl

fun KtExprTempl.withParens(): KtParenExprTempl {
    return KtParenExprTempl(this)
}

class KtTextExprTempl(val text: String) : KtExprTempl() {
    override fun toString(): String = text
}

class KtParenExprTempl(val expr: KtExprTempl) : KtExprTempl() {
    override fun toString(): String = "($expr)"
}

class KtInfixCallExprTempl(val lExpr: KtExprTempl, val rExpr: KtExprTempl, val name: String) : KtExprTempl() {
    override fun toString(): String = "$lExpr $name $rExpr"
}

class KtCallExprTempl(private val qualifier: KtExprTempl?, val name: String, val args: List<KtExprTempl>) : KtExprTempl() {
    override fun toString(): String {
        return buildString {
            if (qualifier != null) {
                append(qualifier)
                append(".")
            }
            append(name)
            append("(")
            append(args.joinToString(", "))
            append(")")
        }
    }
}

sealed class KtStmtTempl : KtTemplIndentable

class KtReturnStmt(val value: KtExprTempl?) : KtStmtTempl() {
    override fun str(indent: Int): String {
        return getIndent(indent) + if (value == null) "return" else "return $value"
    }
}

class KtValStmt(val name: String, val type: String?, val init: KtExprTempl) : KtStmtTempl() {
    override fun str(indent: Int): String {
        return buildString {
            append(getIndent(indent) + "val " + name)
            if (type != null) {
                append(": ")
                append(type)
            }
            append(" = ")
            append(init)
        }
    }
}

private fun getIndent(count: Int): String = buildString {
    for (i in 0 until count) {
        append("  ")
    }
}

enum class ParamKind {
    Simple,
    Val,
    Var
}

class KtParamTempl(val name: String, val type: String, val paramKind: ParamKind = ParamKind.Simple) {
    override fun toString(): String {
        return buildString {
            when (paramKind) {
                ParamKind.Val -> append("val ")
                ParamKind.Var -> append("var ")
                else -> {}
            }
            append(name)
            append(": ")
            append(type)
        }
    }
}

enum class KtModifier {
    Private,
    Inline,
    Override
}

class KtFunTempl(
        val name: String,
        val returnType: String,
        val params: List<KtParamTempl>,
        val statements: List<KtStmtTempl>,
        val modifiers: List<KtModifier> = emptyList(),
        val typeParameters: List<String> = emptyList()
) : KtTemplIndentable, KtClassMemberTempl, KtTopLevelTempl {
    override fun str(indent: Int) : String = buildString {
        append(getIndent(indent))
        if (modifiers.isNotEmpty()) {
            for (modifier in modifiers) {
                append(modifier.toString().toLowerCase() + " ")
            }
        }
        append("fun ")
        if (typeParameters.isNotEmpty()) {
            append(typeParameters.joinToString(", ", prefix = "<", postfix = "> "))
        }
        append("$name(${params.joinToString(", ")}): $returnType {\n")
        append(statements.joinToString(separator = "\n", postfix = "\n") { it.str(indent + 1) })
        append(getIndent(indent) + "}\n")
    }
}

class KtValTempl(val name: String, val type: String, val init: KtExprTempl) : KtTemplIndentable , KtClassMemberTempl{
    override fun str(indent: Int): String {
        return "${getIndent(indent)}val $name: $type = $init"
    }
}

class KtValArrayTempl(val name: String, val type: String, val initList: List<KtExprTempl>) : KtTemplIndentable , KtClassMemberTempl, KtTopLevelTempl{
    override fun str(indent: Int): String {
        return buildString {
            append("${getIndent(indent)}val $name = arrayOf<$type>(\n")
            append(initList.joinToString(",\n", postfix = "\n") { getIndent(indent + 1) + it })
            append(getIndent(indent) + ")")
        }
    }
}

class KtValWithGetter(
        val name: String,
        val returnType: String,
        val statements: List<KtStmtTempl>
) : KtTemplIndentable, KtClassMemberTempl {
    override fun str(indent: Int): String {
        return buildString {
            append(getIndent(indent) + "val $name: $returnType\n")
            append(getIndent(indent + 1) + "get() {\n")
            append(statements.joinToString(separator = "\n", postfix = "\n") { it.str(indent + 2) })
            append(getIndent(indent + 1) + "}\n")
        }
    }
}

interface KtClassMemberTempl : KtTemplIndentable

enum class ClassKind {
    Class,
    EnumClass,
    Object,
    Interface
}

class KtClassTempl(
        val name: String,
        val params: List<KtParamTempl>,
        val members: List<KtClassMemberTempl>,
        val kind: ClassKind = ClassKind.Class,
        val modifiers: List<KtModifier> = listOf()
) : KtTemplIndentable, KtTopLevelTempl {
    override fun str(indent: Int): String {
        return buildString {
            append(getIndent(indent))
            if (modifiers.isNotEmpty()) {
                for (modifier in modifiers) {
                    append(modifier.toString().toLowerCase() + " ")
                }
            }
            append("${kind.toString().toLowerCase()} $name")
            if (params.isNotEmpty()) {
                append("(")
                append(params.joinToString(", "))
                append(")")
            }
            append(" {\n")
            append(members.joinToString(separator = "\n", postfix = "\n") { it.str(indent + 1)})
            append("${getIndent(indent)}}\n")
        }
    }

}

interface KtTopLevelTempl : KtTemplIndentable

class KtFileTempl(
        val topLevelElements: List<KtTopLevelTempl>,
        val packageName: String = "",
        val comment: String = "This is autogenerated file. Not intended for manual editing"
) : KtTempl {
    override fun toString(): String {
        return buildString {
            append("// $comment")
            append("\n")
            append("package ")
            append(packageName)
            append("\n\n")
            append(topLevelElements.joinToString("\n") { it.str(0) })
        }
    }
}

class KtCommentedElementTempl(val element: KtTemplIndentable, private val comment: String) : KtTemplIndentable {
    override fun str(indent: Int): String {
        return getIndent(indent) + "// " + comment + "\n" + element.str(indent)
    }

}