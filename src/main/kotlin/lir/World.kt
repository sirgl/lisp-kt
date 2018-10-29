package lir

import lir.types.TypeStorage

class World(
        val compilationUnits: List<LirCompilationUnit>,
        val typeStorage: TypeStorage
)