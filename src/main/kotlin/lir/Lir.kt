package lir


sealed class LirInstr

class LirMovInstr(val from: Int, val to: Int) : LirInstr() {
    override fun toString(): String {
        return "mov from %$from to %$to"
    }
}

class LirBinInstr(val leftRegister: Int, val rightRegister: Int, val op: LirBinOp) : LirInstr() {
    override fun toString(): String = "binop $op left: %$leftRegister, right: %$rightRegister"
}

enum class LirBinOp {
    Add,
    Sub,
    Mul,
    Div,
    Rem,

    BitAnd,
    BitOr;

    override fun toString(): String = name.toLowerCase()
}

class LirGetFunctionPtrInstr(val name: String, val destReg: Int) : LirInstr() {
    override fun toString(): String {
        return "get_function_ptr $name %$destReg"
    }
}

class LirGetStrPtrInstr(val strIndex: Int, val destReg: Int) : LirInstr() {
    override fun toString(): String {
        return "get_str_ptr strIndex: $strIndex dst: %$destReg"
    }
}

class LirCallInstr(val regArgs: IntArray, val functionName: String, val resultReg: Int) : LirInstr() {
    override fun toString(): String {
        return "call name: $functionName resultReg: %$resultReg args: (${regArgs.joinToString(", ") { "%$it" }})"
    }
}

class LirCondJumpInstr(
    val condReg: Int,
    var thenInstructionIndex: Int = -1,
    var elseInstrIndex: Int = -1
) : LirInstr() {

    override fun toString(): String {
        return "cond_jump cond: %$condReg thenIndex: $thenInstructionIndex elseIndex: $elseInstrIndex"
    }
}

class LirReturnInstr(val reg: Int) : LirInstr() {
    override fun toString(): String {
        return "return %$reg"
    }
}


class LirGotoInstr(var instrIndex: Int = -1) : LirInstr() {

    override fun toString(): String {
        return "goto $instrIndex"
    }
}

class LirInplaceI64(val register: Int, val value: Long) : LirInstr() {
    override fun toString(): String {
        return "inplace_i64 reg: %$register value: $value (without tag: ${value and (0b111 shl 61).inv()})"
    }
}

class LirLoadGlobalVar(val varName: String, val destReg: Int) : LirInstr() {
    override fun toString(): String {
        return ""
    }
}
