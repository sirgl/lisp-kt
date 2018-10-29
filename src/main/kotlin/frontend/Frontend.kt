package frontend

import linting.Lint
import lir.LirCompilationUnit
import lir.World
import util.ResultWithLints

interface Frontend {
    fun run() : ResultWithLints<World>
}