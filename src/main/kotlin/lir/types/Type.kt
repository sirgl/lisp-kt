package lir.types

abstract class Type(val typeDescriptor: TypeDescriptor)

interface TypeDescriptor {
    /**
     * @param name string describing type, must not change and it must be unique for all types
     */
    val descriptor: String

    val presentableName: String
}