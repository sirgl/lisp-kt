package lir.types

class PoiterType(val referredType: Type) : Type(PointerTypeTypeDescriptor(referredType.typeDescriptor))

class PointerTypeTypeDescriptor(val referredTypeDescriptor: TypeDescriptor) : TypeDescriptor {
    override val presentableName: String = "*" + referredTypeDescriptor.presentableName
    override val descriptor: String = "P" + referredTypeDescriptor.descriptor

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PointerTypeTypeDescriptor) return false

        if (referredTypeDescriptor != other.referredTypeDescriptor) return false

        return true
    }

    override fun hashCode(): Int {
        return referredTypeDescriptor.hashCode()
    }
}