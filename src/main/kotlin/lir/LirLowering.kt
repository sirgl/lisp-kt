package lir

import mir.*

class LirFileContext {
    private val stringTable = hashMapOf<String, Int>()
    private val stringList = mutableListOf<String>()

    fun getStrIndex(str: String): Int {
        val index = stringTable[str]
        return if (index == null) {
            val nextIndex = stringList.size
            stringTable[str] = nextIndex
            stringList.add(str)
            nextIndex
        } else {
            index
        }
    }

    fun getStrTable(): Array<String> {
        return stringList.toTypedArray()
    }
}

class LirFunBuilder(val function: MirFunctionDefinition) {
    private val blockIndexToLirInstrIndex: HashMap<Short, Int> = hashMapOf()
    private val instructions = mutableListOf<LirInstr>()

    private var nextRegister: Int = function.varCount.toInt()

    private val idToReg = hashMapOf<MirInstrId, Int>()

    init {
        // assigning register to all values in function
        for (block in function.blocks) {
            val blockIndex = block.index
            for ((index, instruction) in block.instructions.withIndex()) {
                val instrId = MirInstrId(blockIndex, index.toShort())
                if (instruction is MirValueInstr) {
                    idToReg[instrId] = nextRegister()
                }
            }
        }
    }

    fun nextRegister() : Int {
        val register = nextRegister
        nextRegister++
        return register
    }

    fun emit(instr: LirInstr) {
        instructions.add(instr)
    }

    fun startBlock(blockIndex: Short) {
        val index = instructions.size
        blockIndexToLirInstrIndex[blockIndex] = index
    }

    fun getBlockInstructionIndex(blockIndex: Short) : Int {
        return blockIndexToLirInstrIndex[blockIndex]!!
    }

    fun finishFunction() : LirFunction {
        return LirFunction(function.name, instructions, nextRegister, function.parametersCount)
    }

    fun toReg(id: MirInstrId) : Int {
        return idToReg[id]!!
    }
}

object LirLoweringConstants {
    const val INT_TAG = 0b001L
    const val INT_TAG_MASK = INT_TAG shl 61
    const val BOOL_TAG = 0b010L
    const val OBJ_TAG = 0b100L
    const val FUNC_REF_TAG = 0b110L

    // but also anything, but 0 considered as true
    const val TRUE_UNTAGGED = 1L
    const val FALSE_UNTAGGED = 0L

    const val TRUE_TAGGED = TRUE_UNTAGGED and BOOL_TAG
    const val FALSE_TAGGED = FALSE_UNTAGGED and BOOL_TAG

    const val RUNTIME_WITH_ELEMENT_FUNCTION_NAME = "r__withElement"
    const val RUNTIME_CREATE_STRING_FUNCTION_NAME = "r__createString"
    const val RUNTIME_CREATE_SYMBOL_FUNCTION_NAME = "r__createSymbol"
}

class LirLowering {
    fun lower(files: List<MirFile>) : List<LirFile> {
        val world = MirWorld(files)
        return files.map { LirFileLowering(it, world, LirFileContext()).lowerFile() }
    }
}

private class LirFileLowering(val mirFile: MirFile, val world: MirWorld, val context: LirFileContext) {
    fun lowerFile(): LirFile {
        val functions = mirFile.functions
            .filterIsInstance<MirFunctionDefinition>()
            .map { lowerFunction(it) }
        return LirFile(mirFile.source, functions, context.getStrTable())

    }

    private fun lowerFunction(mirFunction: MirFunctionDefinition) : LirFunction {
        // lower all blocks, then put correct labels in another pass
        val builder = LirFunBuilder(mirFunction)
        for (block in mirFunction.blocks) {
            lowerBlock(block, builder)
        }
        val function = builder.finishFunction()
        setupJumpTargets(function, builder)
        return function
    }

    private fun setupJumpTargets(function: LirFunction, builder: LirFunBuilder) {
        for (instruction in function.instructions) {
            when (instruction) {
                is LirGotoInstr -> {
                    // on previous step it was set up
                    val blockIndex = instruction.instrIndex.toShort()
                    val blockInstructionIndex = builder.getBlockInstructionIndex(blockIndex)
                    instruction.instrIndex = blockInstructionIndex
                }
                is LirCondJumpInstr -> {
                    val thenBlockIndex = instruction.thenInstructionIndex.toShort()
                    val elseBlockIndex = instruction.elseInstrIndex.toShort()
                    instruction.thenInstructionIndex = builder.getBlockInstructionIndex(thenBlockIndex)
                    instruction.elseInstrIndex = builder.getBlockInstructionIndex(elseBlockIndex)
                }
            }
        }
    }

    private fun lowerBlock(block: MirBasicBlock, builder: LirFunBuilder) {
        val blockIndex = block.index
        builder.startBlock(blockIndex)
        for ((instrIndexInBlock, instruction) in block.instructions.withIndex()) {
            val instrId = MirInstrId(blockIndex, instrIndexInBlock.toShort())
            when (instruction) {
                is MirGotoInstruction -> {
                    // goto generated only if target is not the following block
                    if (instruction.basicBlockIndex.toInt() != blockIndex + 1) {
                        // index will be patched in another pass
                        builder.emit(LirGotoInstr(instruction.basicBlockIndex.toInt()))
                    }
                }
                is MirCondJumpInstruction -> {
                    val condReg = builder.toReg(instruction.conditionId)
                    // index will be patched in another pass
                    builder.emit(LirCondJumpInstr(
                        condReg,
                        instruction.thenBlockIndex.toInt(),
                        instruction.elseBlockIndex.toInt()
                    ))
                }

                is MirStoreInstr -> {
                    builder.emit(LirMovInstr(builder.toReg(instruction.valueId) , instruction.varId.toInt()))
                }
                is MirLoadInstr -> {
                    builder.emit(LirMovInstr(instruction.varId.toInt(), builder.toReg(instrId)))
                }

                is MirLoadValueInstr -> lowerConstant(instruction, builder, instrId)
                is MirReturnInstruction -> {
                    builder.emit(LirReturnInstr(builder.toReg(instruction.instrValueToReturn)))
                }
                is MirGetFunctionReference -> {
                    val function = world.resolveFunction(instruction.functionId)
                    builder.emit(LirGetFunctionPtrInstr(function.name, builder.toReg(instrId)))
                }
                is MirCallByRefInstr -> {
                    TODO("call by ptr, requires function alignment and function reference tag, " +
                            "before call tag must be deleted")
                }

                is MirLocalCallInstr -> {
                    val function = world.resolveFunction(instruction.functionId)
                    val argRegs = instruction.args.map { builder.toReg(it) }.toIntArray()
                    builder.emit(LirCallInstr(argRegs, function.name, builder.toReg(instrId)))
                }
                is MirWithElementInstr -> {
                    val valueReg = builder.toReg(instruction.valueId)
                    val listReg = builder.toReg(instruction.listId)
                    builder.emit(LirCallInstr(intArrayOf(valueReg, listReg), LirLoweringConstants.RUNTIME_WITH_ELEMENT_FUNCTION_NAME, builder.toReg(instrId)))
                }
                MirAddIntTagInstr -> TODO("bit mask or, opt only")
                MirAddBoolTagInstr -> TODO("bit mask or, opt only")
                MirAddObjTagInstr -> TODO("bit mask or, opt only")
            }
        }
    }

    private fun lowerConstant(instruction: MirLoadValueInstr, builder: LirFunBuilder, instrId: MirInstrId) {
        val value = instruction.value
        when (value) {
            is MirValue.MirInt -> {
                val intConstant = if (value.tagged) {
                    value.value.toLong() or LirLoweringConstants.INT_TAG_MASK
                } else {
                    value.value.toLong()
                }
                builder.emit(LirInplaceI64(builder.toReg(instrId), intConstant))
            }
            is MirValue.MirBool -> {
                val constant = when {
                    value.tagged -> when {
                        value.value -> LirLoweringConstants.TRUE_TAGGED
                        else -> LirLoweringConstants.FALSE_TAGGED
                    }
                    else -> when {
                        value.value -> LirLoweringConstants.TRUE_UNTAGGED
                        else -> LirLoweringConstants.FALSE_UNTAGGED
                    }
                }
                builder.emit(LirInplaceI64(builder.toReg(instrId), constant))
            }
            is MirValue.MirString -> {
                val stringAddrRegister = builder.nextRegister()
                val strIndex = context.getStrIndex(value.value)
                builder.emit(LirGetStrPtrInstr(strIndex, stringAddrRegister))
                builder.emit(LirCallInstr(intArrayOf(stringAddrRegister), LirLoweringConstants.RUNTIME_CREATE_STRING_FUNCTION_NAME, builder.toReg(instrId)))
            }
            is MirValue.MirSymbol -> {
                val stringAddrRegister = builder.nextRegister()
                val strIndex = context.getStrIndex(value.value)
                builder.emit(LirGetStrPtrInstr(strIndex, stringAddrRegister))
                builder.emit(LirCallInstr(intArrayOf(stringAddrRegister), LirLoweringConstants.RUNTIME_CREATE_SYMBOL_FUNCTION_NAME, builder.toReg(instrId)))
            }
            MirValue.MirEmptyList -> {
                builder.emit(LirInplaceI64(builder.toReg(instrId), 0))
            }
        }
    }
}