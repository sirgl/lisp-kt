package hir

import analysis.Handlers
import analysis.Keywords
import analysis.Matchers
import linting.LintSink
import util.Source

val hirExprLowering = Handlers<HirExpr?, HirHandlerContext>().apply {
    addHandler(Keywords.MODULE_KW, Matchers.MODULE) { info, ctx, node ->
        TODO()
    }
}

class HirHandlerContext(
    val source: Source,
    val lintSink: LintSink,
    val isFirst: Boolean,
    val isTopLevel: Boolean,
    val moduleNameRef: Ref<String?>
)

class Ref<T>(var value: T)