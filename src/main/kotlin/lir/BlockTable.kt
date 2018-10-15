package lir

class BlockTable {
    var nextBlockIndex = 0
    val blocks = mutableListOf<BasicBlock>()

    fun addBlock(block: BasicBlock) : BlockId {
        blocks[nextBlockIndex] = block
        val blockId = BlockId(nextBlockIndex)
        block.id = blockId
        nextBlockIndex++
        return blockId
    }

    operator fun get(index: BlockId) : BasicBlock {
        return blocks[index.index]
    }
}