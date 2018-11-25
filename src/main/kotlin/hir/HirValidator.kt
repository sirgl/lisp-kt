package hir

import linting.Lint
import linting.LintSink
import linting.Severity
import linting.Subsystem
import parser.TextRange
import util.Source

class HirValidator {
    fun validate(hirFiles: List<HirFile>, lintSink: LintSink) {
        for (hirFile in hirFiles) {
            CapturingVariableValidator(lintSink, hirFile).validateFile()
        }
    }
}

class CapturingVariableValidator(private val lintSink: LintSink, val file: HirFile) {
    val source: Source = file.source
    fun validateFile() {
        val allowedReferences = HashSet<HirVarDeclaration>()
        bypass(file) {
            when (it) {
                is HirVarDeclaration -> {
                    allowedReferences.add(it)
                }
                is HirVarReference -> {
                    validateNameAllowed(allowedReferences, it.decl)
                }
                is HirAssignExpr -> {
                    validateNameAllowed(allowedReferences, it.decl)
                }
            }
        }
    }

    private fun bypass(node: HirNode, consumer: (HirNode) -> Unit) {
        consumer(node)
        for (child in node.children) {
            bypass(child, consumer)
        }
    }

    private fun validateNameAllowed(allowedReferences: HashSet<HirVarDeclaration>, declaration: HirVarDeclaration) {
        if (declaration !in allowedReferences) {
            lintSink.addLint(Lint("The function should not capture variables from context: ${declaration.name}",
                    TextRange(0, 0), Severity.Error, Subsystem.LoweringToHir, source))
        }
    }

}