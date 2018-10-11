package lir.types

class TypeStorage {
    private val types = mutableListOf<LirType>()
    private val typeToIndex = mutableMapOf<LirType, Int>()

    fun getDescriptor(type: LirType) : LirTypeDescr {
        val index = typeToIndex[type] ?: addType(type)
        return LirTypeDescr(index)
    }

    private fun addType(type: LirType): Int {
        val newIndex = types.size
        types.add(type)
        typeToIndex[type] = newIndex
        return newIndex
    }
}

/**
 * Holds index of type in table
 */
inline class LirTypeDescr(val index: Int)