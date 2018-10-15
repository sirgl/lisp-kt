package lir.types

import util.collection.IntList

class TypeStorage {
    private val types = mutableListOf<LirType>()
    private val typeToIndex = mutableMapOf<LirType, Int>()

    fun getDescriptor(type: LirType) : LirTypeIndex {
        val index = typeToIndex[type] ?: addType(type)
        return LirTypeIndex(index)
    }

    fun addType(type: LirType): Int {
        val newIndex = types.size
        types.add(type)
        typeToIndex[type] = newIndex
        return newIndex
    }

    operator fun get(index: Int): LirType = types[index]
}

/**
 * Holds index of type in table
 */
inline class LirTypeIndex(val index: Int)

inline class TypeIndexList(val list: IntList) {
    val size: Int
        get() = list.size

    operator fun get(index: Int): Int = list[index]
}