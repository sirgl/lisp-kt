package backend

class TargetPlatform(
        val osType: OsType,
        val instructionSet: InstructionSetType
)

enum class OsType {
    Linux
}

enum class InstructionSetType {
    X64
}