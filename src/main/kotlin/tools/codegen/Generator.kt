package tools.codegen

class GeneratedData(
        val classWithAccessors: KtClassTempl,
        val constructor: KtFunTempl,
        val extension: KtFunTempl
)

// TODO it would be better to generate constructors by layout
class AccessorsAndConstructorsGenerator(private val descr: InstructionDescription) {
    private val titledName = "Instruction${descr.lowercasedName.toTitle()}"

    fun generateInstructionData(): GeneratedData {
        val classWithAccessors = generateClass()
        val constructor = generateConstructorFunction()
        return GeneratedData(classWithAccessors, constructor, generateExtension())
    }

    private fun generateExtension() : KtFunTempl {
        val instructionClass = KtCallExprTempl(null, titledName, listOf(KtTextExprTempl("this.storage")))
        val action = KtCallExprTempl(instructionClass, "block", listOf())
        return KtFunTempl(
                "BBInstruction.as${descr.lowercasedName.toTitle()}",
                "T",
                listOf(KtParamTempl("block", "$titledName.()->T")),
                listOf(KtReturnStmt(action)),
                listOf(KtFunModifier.Inline),
                listOf("T")
        )
    }

    private fun generateConstructorFunction(): KtFunTempl {
        val statements = mutableListOf<KtStmtTempl>()
        var prevIndex = 0
        statements.add(KtValStmt("v0", "Long", KtTextExprTempl("0")))
        var offsetFromPreviousSet = 0
        for (value in descr.instructionLayout.values) {
            if (value.generationNeeded) {
                val paramCast = KtCallExprTempl(KtTextExprTempl(value.propertyName), "toLong", listOf())
                val previous = KtTextExprTempl("v$prevIndex")
                val offsetExpr = KtTextExprTempl((offsetFromPreviousSet * 8).toString())
                val shifted = KtInfixCallExprTempl(previous, offsetExpr, "shr").withParens()
                val init = KtInfixCallExprTempl(shifted, paramCast, "or")
                statements.add(KtValStmt("v${prevIndex + 1}", "Long", init))
                prevIndex++
                offsetFromPreviousSet = 0
            }
            offsetFromPreviousSet += value.size
        }
        val instruction = KtCallExprTempl(null, "BBInstruction", listOf(KtTextExprTempl("v$prevIndex")))
        statements.add(KtReturnStmt(instruction))
        val params = descr.instructionLayout.values
                .filter { it.generationNeeded }
                .map { KtParamTempl(it.propertyName, it.typeName) }
        return KtFunTempl("construct$titledName", "BBInstruction", params, statements)
    }

    private fun generateClass(): KtClassTempl {
        val members = mutableListOf<KtClassMemberTempl>()

        var offset = 0

        // fun f(a, b, c) { val v0: Long = 0; val v1 = v0 shl sizeofFirst }
        for (value in descr.instructionLayout.values) {
            if (value.generationNeeded) {

                members.add(KtValWithGetter(
                        value.propertyName,
                        value.typeName,
                        listOf(KtReturnStmt(generateReadExpr(value.size, offset, "storage")))
                ))
            }
            offset += value.size
        }
        return KtClassTempl(titledName, listOf(KtParamTempl("storage", "Long", ParamKind.Val)), members)
    }


    fun generateReadExpr(sizeBytes: Int, startOffsetBytes: Int, storageName: String): KtCallExprTempl {
        val offsetFromRight = generateOffsetFromRight(sizeBytes, startOffsetBytes)
        val shifted = KtInfixCallExprTempl(KtTextExprTempl(storageName), offsetFromRight, "shr").withParens()
        val conversionFunction = when (sizeBytes) {
            1 -> "toByte"
            2 -> "toShort"
            4 -> "toInt"
            else -> throw UnsupportedOperationException()
        }
        return KtCallExprTempl(shifted, conversionFunction, emptyList())
    }

    //    /**
//     * @param startOffsetBytes start offset in long starting from left
//     */
//    fun generateMask(sizeBytes: Int, startOffsetBytes: Int): KtCallExprTempl {
//        val invertedMask = "0x" + when (sizeBytes) {
//            1 -> Byte.MAX_VALUE.toString(16)
//            2 -> Short.MAX_VALUE.toString(16)
//            4 -> Int.MAX_VALUE.toString(16)
//            else -> throw UnsupportedOperationException()
//        }
//        val invertedMaskExpr = KtLiteralTempl(invertedMask)
//        val invertedMaskLongExpr = KtCallExprTempl(invertedMaskExpr, "toLong", listOf())
//        val offsetExpr = generateOffsetFromRight(sizeBytes, startOffsetBytes)
//        val maskLongExprShifted = KtInfixCallExprTempl(invertedMaskLongExpr, offsetExpr,"shl").withParens()
//        return KtCallExprTempl(maskLongExprShifted, "inv", listOf())
//    }
//
    private fun generateOffsetFromRight(sizeBytes: Int, startOffsetBytes: Int): KtTextExprTempl {
        val offsetFromEnd = 8 - startOffsetBytes - sizeBytes
        val offsetFromEndBits = offsetFromEnd * 8
        return KtTextExprTempl(offsetFromEndBits.toString())
    }

//    // value.toLong() shl 16
//    fun generateShiftedValue(sizeBytes: Int, startOffsetBytes: Int) {
//        val offsetFromRight = generateOffsetFromRight(sizeBytes, startOffsetBytes)
//
//    }
}


fun main(args: Array<String>) {
    val topLevelElements = mutableListOf<KtTopLevelTempl>()
    for (instruction in Instructions.instructions) {
        val generatedData = AccessorsAndConstructorsGenerator(instruction).generateInstructionData()
        topLevelElements.add(generatedData.classWithAccessors)
        topLevelElements.add(generatedData.constructor)
        topLevelElements.add(generatedData.extension)
    }
    val file = KtFileTempl(topLevelElements, "lir")
    println(file)

}

private fun String.toTitle(): String {
    return this[0].toUpperCase().toString() + this.substring(1)
}