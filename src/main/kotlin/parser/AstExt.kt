package parser

fun AstNode.prettyPrint(): String {
    val sb = StringBuilder()
    prettyPrint(sb)
    return sb.toString()
}

private fun AstNode.prettyPrint(sb: StringBuilder, level: Int = 0) {
    pad(sb, level)
    sb.append(toString())
    if (this is ListNode) {
        for (child in children) {
            sb.append("\n")
            child.prettyPrint(sb, level + 1)
        }
    }
}

private fun pad(sb: StringBuilder, level: Int) {
    for (i in 0 until level) {
        sb.append("  ")
    }
}