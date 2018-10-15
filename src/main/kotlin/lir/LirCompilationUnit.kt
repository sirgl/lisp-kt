package lir

import util.io.Path

class LirCompilationUnit(
        val sourceFile: Path,
        val functions: List<LirFunction>,
        val constantTable: ConstantTable
) {

    override fun toString(): String {
        TODO("print llir")
    }
}