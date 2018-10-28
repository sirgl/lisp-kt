package tools.codegen

import java.io.File

private val objectName = "Instructions"
private val instructionInlineClass = "BBInstruction"

fun generateInstructionsFile() : KtFileTempl {
    val topLevelElements = mutableListOf<KtTopLevelTempl>()
    for (instruction in InstructionDescriptions.instructions) {
        val generatedData = AccessorsAndConstructorsGenerator(instruction).generateInstructionData()
        topLevelElements.add(generatedData.classWithAccessors)
        topLevelElements.add(generatedData.constructor)
        topLevelElements.add(generatedData.extensionWith)
        topLevelElements.add(generatedData.extensionAs)
    }

    val instructionsObject = createInstructionObject()
    topLevelElements.add(instructionsObject)

    return KtFileTempl(topLevelElements, "lir")
}

private fun createInstructionObject(): KtClassTempl {
    val members = mutableListOf<KtClassMemberTempl>()
    for (instruction in InstructionDescriptions.instructions) {
        members.add(KtValTempl(instruction.constantName, "Byte", KtTextExprTempl(instruction.opcode.toString())))
    }
    members.add(generateToStringArray())
    return KtClassTempl(
            name = objectName,
            params = listOf(),
            members = members,
            kind = ClassKind.Object
    )
}

private fun generateToStringArray() : KtValArrayTempl {
    val lambdas = InstructionDescriptions.instructions
            .map { KtTextExprTempl("{ it.as${it.titledName}().toString() }") }
    return KtValArrayTempl("toStrings", "(BBInstruction)->String", lambdas)
}

internal class InstructionData(
        val classWithAccessors: KtClassTempl,
        val constructor: KtFunTempl,
        val extensionWith: KtFunTempl,
        val extensionAs: KtFunTempl
)

// TODO it would be better to generate constructors by layout
class AccessorsAndConstructorsGenerator(private val descr: InstructionDescription) {
    private val className = "Instruction${descr.name.joinToString("") { it.toTitle() }}"

    internal fun generateInstructionData(): InstructionData {
        return InstructionData(
                classWithAccessors = generateClass(),
                constructor = generateConstructorFunction(),
                extensionWith = generateExtensionWith(),
                extensionAs = generateExtensionAs()
        )
    }

    private fun generateExtensionWith() : KtFunTempl {
        val instructionClass = KtCallExprTempl(null, className, listOf(KtTextExprTempl("this.storage")))
        val action = KtCallExprTempl(instructionClass, "block", listOf())
        return KtFunTempl(
                "$instructionInlineClass.with${descr.titledName}",
                "T",
                listOf(KtParamTempl("block", "$className.()->T")),
                listOf(KtReturnStmt(action)),
                listOf(KtModifier.Inline),
                listOf("T")
        )
    }

    private fun generateExtensionAs() : KtFunTempl {
        val instructionConstr = KtCallExprTempl(null, className, listOf(KtTextExprTempl("this.storage")))
        return KtFunTempl(
                "$instructionInlineClass.as${descr.titledName}",
                className,
                listOf(),
                listOf(KtReturnStmt(instructionConstr)),
                listOf(KtModifier.Inline)
        )
    }

    private fun generateConstructorFunction(): KtFunTempl {
        val statements = mutableListOf<KtStmtTempl>()
        var prevIndex = 0
        statements.add(KtValStmt("v0", "Long", KtTextExprTempl("$objectName." + descr.constantName + ".toLong()")))
        var emptySizeInTail = 0
        var isFirst = true
        for (value in descr.instructionLayout.values) {
            // Is required for omitting opcode generation
            if (isFirst) {
                isFirst = false
                continue
            }
            if (value.generationNeeded) {
                val paramCast = KtCallExprTempl(KtTextExprTempl(value.propertyName), "toLong", listOf())
                val previous = KtTextExprTempl("v$prevIndex")
                val offsetExpr = KtTextExprTempl(((emptySizeInTail + value.size) * 8).toString())
                val shifted = KtInfixCallExprTempl(previous, offsetExpr, "shl").withParens()
                val init = KtInfixCallExprTempl(shifted, paramCast, "or")
                statements.add(KtValStmt("v${prevIndex + 1}", "Long", init))
                prevIndex++
                emptySizeInTail = 0
            } else {
                emptySizeInTail += value.size
            }
        }
        // If empty in the end, shifting
        if (emptySizeInTail != 0) {
            val offset = KtTextExprTempl((emptySizeInTail * 8).toString())
            val prev = KtInfixCallExprTempl(KtTextExprTempl("v$prevIndex"), offset, "shl")
            statements.add(KtValStmt("v${prevIndex + 1}", "Long", prev))
            prevIndex++
        }
        val instruction = KtCallExprTempl(null, instructionInlineClass, listOf(KtTextExprTempl("v$prevIndex")))
        statements.add(KtReturnStmt(instruction))
        val params = descr.instructionLayout.values
                .drop(1) // skipping opcode
                .filter { it.generationNeeded }
                .map { KtParamTempl(it.propertyName, it.typeName) }
        return KtFunTempl("construct${descr.titledName}", instructionInlineClass, params, statements)
    }

    private fun generateClass(): KtClassTempl {
        val members = mutableListOf<KtClassMemberTempl>()
        var offset = 0
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
        members.add(generateToString())
        return KtClassTempl(
                name = className,
                params = listOf(KtParamTempl("storage", "Long", ParamKind.Val)),
                members = members,
                kind = ClassKind.Class,
                modifiers = listOf(KtModifier.Inline)
        )
    }

    private fun generateToString() : KtFunTempl {
        val properties = descr.instructionLayout.values
                .drop(1)
                .filter { it.generationNeeded }
                .joinToString { "${it.propertyName}=${toStringPropertyRenderer(it)}" }
        val value = KtTextExprTempl("\"${descr.snakeName} $properties\"")
        return KtFunTempl(
                "toString",
                "String",
                listOf(),
                listOf(KtReturnStmt(value)),
                listOf(KtModifier.Override)
        )
    }

    private fun toStringPropertyRenderer(it: InstructionInlineValue): String = when (it) {
        is OperandValue -> "%\$" + it.propertyName
        else -> "\$" + it.propertyName
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
    val instructionsFile = generateInstructionsFile()
    // TODO avoid using java api
    File("src/main/kotlin/lir/Instructions.kt").writeText(instructionsFile.toString())

}

internal fun String.toTitle(): String {
    return this[0].toUpperCase().toString() + this.substring(1)
}