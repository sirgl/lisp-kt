package deps

import linting.LintSink
import linting.Lint
import linting.Severity
import linting.Subsystem


class DependencyValidator {
    fun validateDependencies(dependencies: List<DependencyEntry>, lintSink: LintSink) {
        for (dependency in dependencies) {
            for (childDep in dependency.dependencies) {
                if (childDep is UnsatisfiedDependencyEntry) {
                    val ast = (dependency as RealDependencyEntry).ast
                    lintSink.addLint(Lint("Unsatisfied dependency: ${childDep.name}", ast.root.textRange, Severity.Error, Subsystem.Verification, ast.source))
                }
            }
        }
    }
}
