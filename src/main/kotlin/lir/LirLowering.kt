package lir

import mir.*

class LirLowering {
    fun lower(world: MirWorld) {

    }
}

class LirUnitLowering(val mirFile: MirFile) {
    fun lowerFunction(mirFunction: MirFunctionDefinition) : LirFunction {
//        mirFunction.
        TODO()
    }

    fun lowerBlock(block: MirBasicBlock) {
        for (instruction in block.instructions) {
            when (instruction) {

                is MirGotoInstruction -> TODO()
                is MirCondJumpInstruction -> TODO()

                is MirStoreInstr -> TODO()
                is MirLoadInstr -> TODO()

                is MirLoadValueInstr -> {
                    val value = instruction.value
                    when (value) {
                        is MirValue.MirInt -> TODO("load tagged/untagged int")
                        is MirValue.MirBool -> TODO("load tagged/untagged true/false")
                        is MirValue.MirString -> TODO("call to runtime, add cstring to constants of file, load cstring")
                        is MirValue.MirSymbol -> TODO("call to runtime, add cstring to constants of file, load cstring")
                        MirValue.MirEmptyList -> TODO("load i64 0")
                    }
                }
                is MirReturnInstruction -> TODO("return tail instruction")
                is MirGetFunctionReference -> TODO("function ptr, locate function index")
                is MirCallByRefInstr -> TODO("call by ptr, requires function alignment and function reference tag, " +
                        "before call tag must be deleted")

                is MirLocalCallInstr -> TODO("replace with LIR call, put arguments in args instructions if required, " +
                        "locate function index")
                is MirWithElementInstr -> TODO("call to runtime")
                MirAddIntTagInstr -> TODO("bit mask or, opt only")
                MirAddBoolTagInstr -> TODO("bit mask or, opt only")
                MirAddObjTagInstr -> TODO("bit mask or, opt only")
            }
        }
    }
}