package mir

class MirBuilderContext(
        private var nextFunctionId : Int = 0
) {
    fun nextFunctionId() : Int {
        return nextFunctionId
    }
}

class MirFunctionBuilder(val name: String, val isMain: Boolean = false, val context: MirBuilderContext) {
    var currentBasicBlock = mutableListOf<MirInstr>()
    val blocks = mutableListOf<MirBasicBlock>()
    var nextBlockIndex: Short = 0


    fun emit(instr: MirInstr) : MirInstrId {
        currentBasicBlock.add(instr)
        return MirInstrId(blocks.size.toShort(), (currentBasicBlock.size - 1).toShort())
    }

    /**
     * @return id of finished block
     */
    fun finishBlock() : Short {
        val index = nextBlockIndex
        blocks.add(MirBasicBlock(index, currentBasicBlock.toMutableList()))
        currentBasicBlock.clear()
        nextBlockIndex++
        return index
    }

    fun finishFunction() : MirFunction {
        val function = MirFunction(name, blocks, 0, isMain)
        function.functionId = context.nextFunctionId()
        return function
    }
}