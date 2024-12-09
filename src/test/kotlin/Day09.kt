import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInputDay09 = "2333133121414131402"

class Day09Part1: BehaviorSpec() { init {

    Given("simple example") {
        val blocks = parseDiskBlocks("12345")
        Then("should have right blocks") {
            blocks shouldBe listOf(
                Block(0,  1, false),
                Block(-1, 2, true),
                Block(1, 3, false),
                Block(-1, 4, true),
                Block(2, 5, false)
            )
        }
        Then("should print blocks") {
            printBlocks(blocks) shouldBe "0..111....22222"
        }
        When("compacting blocks") {
            val compacted = compactBlocks(blocks)
            Then("should be correctly compacted") {
                printBlocks(compacted) shouldBe "022111222......"
            }
        }
    }
    Given("more examples") {
        When("the last block fits completely in a free block at the begining") {
            val example = parseDiskBlocks("12122")
            printBlocks(example) shouldBe "0..1..22"
            Then("should be compacted correctly") {
                printBlocks(compactBlocks(example)) shouldBe "0221...."
            }
        }
        When("the last block doesn't fit completely in a free block at the begining") {
            val example = parseDiskBlocks("12123")
            printBlocks(example) shouldBe "0..1..222"
            Then("should be compacted correctly") {
                printBlocks(compactBlocks(example)) shouldBe "02212...."
            }
        }
        When("the last block is smaller than the free block at the begining") {
            val example = parseDiskBlocks("12121")
            printBlocks(example) shouldBe "0..1..2"
            Then("should be compacted correctly") {
                printBlocks(compactBlocks(example)) shouldBe "021...."
            }
        }
    }
    Given("example input") {
        val blocks = parseDiskBlocks(exampleInputDay09)
        Then("should be parsed correctly") {
            blocks.size shouldBe 18
            printBlocks(blocks) shouldBe "00...111...2...333.44.5555.6666.777.888899"
        }
        When("compacting") {
            val compacted = compactBlocks(blocks)
            Then("should be compacted correctly") {
                printBlocks(compacted) shouldBe "0099811188827773336446555566.............."
            }
            Then("should have the right checksum") {
                sumIds(compacted) shouldBe 1928L
            }
        }
    }

    Given("exercise input") {
        val blocks = parseDiskBlocks(readResource("inputDay09.txt")!!)
        When("compacting") {
            val compacted = compactBlocks(blocks)

            Then("should have the right checksum") {
                sumIds(compacted) shouldBe 6_366_665_108_136L
            }
        }
    }
} }

class Day09Part2: BehaviorSpec() { init {

    Given("more examples") {
        When("the last block fits completely in a free block at the begining") {
            val example = parseDiskBlocks("12122")
            printBlocks(example) shouldBe "0..1..22"
            Then("should be compacted correctly") {
                printBlocks(compactBlocks2(example)) shouldBe "0221...."
            }
        }
        When("the last block doesn't fit completely in a free block at the begining") {
            val example = parseDiskBlocks("12123")
            printBlocks(example) shouldBe "0..1..222"
            Then("should not move last block") {
                printBlocks(compactBlocks2(example)) shouldBe "01....222"
            }
        }
        When("the last block is smaller than the free block at the begining") {
            val example = parseDiskBlocks("12121")
            printBlocks(example) shouldBe "0..1..2"
            Then("should be compacted correctly") {
                printBlocks(compactBlocks2(example)) shouldBe "021...."
            }
        }
    }
    Given("example input") {
        val blocks = parseDiskBlocks(exampleInputDay09)
        When("compacting") {
            val compacted = compactBlocks2(blocks)
            Then("should be compacted correctly") {
                printBlocks(compacted) shouldBe "00992111777.44.333....5555.6666.....8888.."
            }
            Then("should have the right checksum") {
                sumIds(compacted) shouldBe 2858L
            }
        }
    }

    Given("exercise input") {
        val blocks = parseDiskBlocks(readResource("inputDay09.txt")!!)
        Then("sum of all block sizes") {
            blocks.map { it.size }.sum() shouldBe 94935

        }
        When("compacting") {
            val compacted = compactBlocks2(blocks)

            Then("should have the right checksum") {
                sumIds(compacted) shouldBe 6_398_065_450_842L
            }
        }

    }
} }

private fun parseDiskBlocks(input: String): List<Block> {
    var empty = false
    var id = 0
    return input.mapIndexedNotNull() { i, c ->
        val length = c.toString().toInt()
        val result = if (! (empty && length == 0)) // empty block with size 0 not in result
            Block(if (empty) -1 else id, length, empty)
        else null
        if (!empty) id++ // count only not empty blocks
        empty = !empty // empty blocks alternate
        result
    }
}

private fun printBlocks(blocks: List<Block>): String = sequence {
    blocks.forEach { with(it) {
      repeat(size) {
          yield(if (empty) '.' else id)
      }
    } }
}.joinToString("")


private fun sumIds(blocks: List<Block>): Long =
    blocks.flatMap { with(it) {
            List(size) { if (empty) 0 else id.toLong() }
        }
    }.mapIndexed { i, size ->
        i * size
    }.sum()

private fun compactBlocks(blocks: List<Block>): List<Block> {
    val compactedBlocks  = mutableListOf<Block>()
    val freeBlocks = mutableListOf<Block>()
    var fromStart = 0
    var fromEnd = blocks.size - 1
    var fromStartBlock: Block? = null
    var fromEndBlock: Block? = null
    while (fromStart < fromEnd) {
        if (fromStartBlock == null) fromStartBlock = blocks[fromStart]
        if (fromEndBlock == null) fromEndBlock = blocks[fromEnd]
        if (! fromStartBlock.empty)  { // keep non-empty blocks from the beginning
            compactedBlocks.add(fromStartBlock)
            fromStartBlock = null
            fromStart++
        } else { // empty
            if (fromEndBlock.empty) { // keep empty blocks from the end
                freeBlocks.add(fromEndBlock)
                fromEndBlock = null
                fromEnd--
            } else {
                if (fromEndBlock.size == fromStartBlock.size) { // empty block from beging matches exactly
                    compactedBlocks.add(fromEndBlock)
                    freeBlocks.add(fromStartBlock)
                    fromStartBlock = null
                    fromStart++
                    fromEndBlock = null
                    fromEnd--
                } else if (fromEndBlock.size < fromStartBlock.size) { // block smaller than free space
                    compactedBlocks.add(Block(fromEndBlock.id, fromEndBlock.size, false))
                    freeBlocks.add(Block(-1, fromEndBlock.size, true))
                    fromStartBlock = fromStartBlock.copy(size = fromStartBlock.size - fromEndBlock.size)
                    fromEndBlock = null
                    fromEnd--
                } else { // empty block not big enough
                    compactedBlocks.add(Block(fromEndBlock.id, fromStartBlock.size, false))
                    freeBlocks.add(Block(-1, fromStartBlock.size, true))
                    fromEndBlock = fromEndBlock.copy(size = fromEndBlock.size - fromStartBlock.size)
                    fromStartBlock = null
                    fromStart++
                }
            }
        }
    }
    // Handle remaining blocks
    if(fromStartBlock != null)
        if (fromStartBlock.empty) freeBlocks.add(fromStartBlock)
        else compactedBlocks.add(fromStartBlock)
    if(fromEndBlock != null)
        if (fromEndBlock.empty) freeBlocks.add(fromEndBlock)
        else compactedBlocks.add(fromEndBlock)
    return compactedBlocks + freeBlocks
}

private fun compactBlocks2(blocks: List<Block>): List<Block> {
    var idLastBlockChecked = Int.MAX_VALUE
    var currentBlocks = blocks
    val movedBlocks = mutableSetOf<Int>()
    while (true) {
        val indexBlockToMove = currentBlocks.indexOfLast { !it.empty && it.id < idLastBlockChecked }
        if (indexBlockToMove < 0) return currentBlocks
        val blockToMove = currentBlocks[indexBlockToMove]
        idLastBlockChecked = blockToMove.id
        if (movedBlocks.contains(blockToMove.id)) return currentBlocks // block to move has already been moved
        val indexMoveTo = currentBlocks.indexOfFirst { it.empty && it.size >= blockToMove.size }
        if (indexMoveTo >= 0 && indexMoveTo < indexBlockToMove) {
            val blockMoveTo = currentBlocks[indexMoveTo]
            // Move block
            val nextBlocks = sequence {
                currentBlocks.forEachIndexed { i, block ->
                    if (i == indexMoveTo) {
                        yield(blockToMove)
                        if (blockToMove.size < blockMoveTo.size)
                            yield(Block(-1, blockMoveTo.size - blockToMove.size, true))
                    } else if (i == indexBlockToMove) {
                        yield(Block(-1, blockToMove.size, true))
                    } else yield(block)
                }
            }.toList()
            movedBlocks.add(blockToMove.id)
            currentBlocks = nextBlocks
        }
    }
}

private data class Block(val id: Int, val size: Int, val empty: Boolean)

