package lir

import lir.types.I32TypeDescriptor
import lir.types.I8TypeDescriptor
import lir.types.TypeDescriptor

val i32Matcher = SingleTypeDescriptorMatcher(I32TypeDescriptor)
val i8Matcher = SingleTypeDescriptorMatcher(I8TypeDescriptor)

class SingleTypeDescriptorMatcher(val typeDescriptor: TypeDescriptor) : TypeDescriptorMatcher {
    override val humanReadableTypeRequirement: String
        get() = "Type must be exactly ${typeDescriptor.presentableName}"

    override fun matchTypeDescriptor(descriptor: TypeDescriptor) = when (descriptor) {
        typeDescriptor -> TypeDescriptorMatchResult.Success
        else -> TypeDescriptorMatchResult.Failure(humanReadableTypeRequirement)
    }
}

interface TypeDescriptorMatcher {
    fun matchTypeDescriptor(descriptor: TypeDescriptor) : TypeDescriptorMatchResult

    val humanReadableTypeRequirement: String
}

sealed class TypeDescriptorMatchResult {
    object Success : TypeDescriptorMatchResult()
    class Failure(val message: String) : TypeDescriptorMatchResult()
}
