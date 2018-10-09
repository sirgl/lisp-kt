package backend

abstract class Assembler(
        val dialect: AssemblyDialect,
        val targetPlatform: TargetPlatform
)

enum class AssemblyDialect {
    Gas
}