package mir


class MirWorld(val files: List<MirFile>) : MirFunctionResolver {
    override fun resolveFunction(id: Int): MirFunction {
        for (file in files) {
            for (function in file.functions) {
                if (function.functionId == id) {
                    return function
                }
            }
        }
        throw NoSuchElementException()
    }
}

interface MirFunctionResolver {
    fun resolveFunction(id: Int) : MirFunction
}