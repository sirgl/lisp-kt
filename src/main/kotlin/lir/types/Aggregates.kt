package lir.types

abstract class AggregateType(typeDescriptor: TypeDescriptor) : Type(typeDescriptor)

interface AggregateTypeDescriptor : TypeDescriptor

class StructTypeDescriptor(val structFieldTypeDescriptors: Array<TypeDescriptor>) : TypeDescriptor {
    // TODO lazy?
    override val presentableName: String = "struct(" + structFieldTypeDescriptors.joinToString { it.presentableName } + ")"
    override val descriptor: String = "S" + structFieldTypeDescriptors.joinToString { it.descriptor }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StructTypeDescriptor) return false

        if (!structFieldTypeDescriptors.contentEquals(other.structFieldTypeDescriptors)) return false

        return true
    }

    override fun hashCode(): Int {
        return structFieldTypeDescriptors.contentHashCode()
    }
}

class StructType(
        val fields: Array<StructField>,
        val layout: StructLayout
) : AggregateType(StructTypeDescriptor(fields.map { it.type.typeDescriptor }.toTypedArray()))

class StructField(
        val name: String,
        val type: Type
)

interface StructLayout {
    /**
     * @return offsets for each field
     */
    fun doLayout(fields: Array<StructField>) : IntArray
}



class ArrayType(
        val elementType: Type
) : AggregateType(ArrayTypeDescriptor(elementType.typeDescriptor))

class ArrayTypeDescriptor(val elementTypeDescriptor: TypeDescriptor) : AggregateTypeDescriptor {
    override val presentableName: String = "[" + elementTypeDescriptor.presentableName + "]"
    override val descriptor: String = "A" + elementTypeDescriptor.descriptor

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArrayTypeDescriptor) return false

        if (elementTypeDescriptor != other.elementTypeDescriptor) return false

        return true
    }

    override fun hashCode(): Int {
        return elementTypeDescriptor.hashCode()
    }
}