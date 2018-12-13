package mir

import hir.HirFunctionDeclaration
import hir.HirFunctionDefinition
import hir.HirVarDeclaration

class MirBuilderContext(
        private var nextFunctionId: Int = 0

) {
    private val functionToId: MutableMap<HirFunctionDeclaration, Int> = hashMapOf()

    fun addFunction(function: HirFunctionDeclaration, id: Int) {
        functionToId[function] = id
    }

    fun getFunctionId(function: HirFunctionDeclaration) : Int {
        return functionToId[function]!!
    }

    fun nextFunctionId(): Int {
        return nextFunctionId++
    }
}

class MirFunctionBuilder(val name: String, val isMain: Boolean = false, val context: MirBuilderContext) {
    private var currentBasicBlock = mutableListOf<MirInstr>()
    private val blocks = mutableListOf<MirBasicBlock>()
    private var nextBlockIndex: Short = 0
    private val varTable: MutableMap<HirVarDeclaration, Short> = hashMapOf()
    private var nextVarIndex: Short = 0
    private var nextIfMergeIndex = 0
    private val varNameTable = hashMapOf<Short, String>()

    fun addVariable(declaration: HirVarDeclaration): Short {
        varTable[declaration] = nextVarIndex
        varNameTable[nextVarIndex] = declaration.name
        nextVarIndex++
        return nextVarIndex
    }

    fun getVarId(declaration: HirVarDeclaration): Short {
        return varTable[declaration]!! // can be done, as validation already passed and it is known, that declaration exist

    }

    fun emit(instr: MirInstr): MirInstrId {
        currentBasicBlock.add(instr)
        return MirInstrId(currentBlockId(), (currentBasicBlock.size - 1).toShort())
    }

    /**
     * @return id of finished block
     */
    fun finishBlock(): Short {
        val index = nextBlockIndex
        blocks.add(MirBasicBlock(index, currentBasicBlock.toMutableList()))
        currentBasicBlock.clear()
        nextBlockIndex++
        return index
    }

    fun finishFunction(hirFunction: HirFunctionDeclaration, satellite: Boolean = false): MirFunction {
        val function = MirFunctionDefinition(
            name,
            blocks,
            0,
            hirFunction.parameters.size,
            varTable.size.toShort(),
            varNameTable.toMap(),
            isMain,
            satellite
        )
        varNameTable.clear()
        function.functionId = context.nextFunctionId()
        context.addFunction(hirFunction, function.functionId)
        return function
    }

    fun getFunctionId(function: HirFunctionDeclaration) : Int {
        return context.getFunctionId(function)
    }

    fun currentBlockId(): Short {
        return (nextBlockIndex)
    }

    fun nextIfMergeVarName(): String {
        val index = nextIfMergeIndex
        nextIfMergeIndex++
        return "__merge_if_$index"
    }
}