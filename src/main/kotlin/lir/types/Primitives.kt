package lir.types

abstract class PrimitiveType(typeDescriptor: PrimitiveTypeDescriptor) : Type(typeDescriptor)

interface PrimitiveTypeDescriptor : TypeDescriptor

object I32Type : PrimitiveType(I32TypeDescriptor)

object I32TypeDescriptor : PrimitiveTypeDescriptor {
    override val presentableName: String
        get() = "i32"
    override val descriptor: String
        get() = "Li32"
}

object I8Type : PrimitiveType(I8TypeDescriptor)

object I8TypeDescriptor : PrimitiveTypeDescriptor {
    override val presentableName: String
        get() = "i8"
    override val descriptor: String
        get() = "Li8"
}
