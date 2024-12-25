
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInputDay25 = """
            #####
            .####
            .####
            .####
            .#.#.
            .#...
            .....
            
            #####
            ##.##
            .#.##
            ...##
            ...#.
            ...#.
            .....
            
            .....
            #....
            #....
            #...#
            #.#.#
            #.###
            #####
            
            .....
            .....
            #.#..
            ###..
            ###.#
            ###.#
            #####
            
            .....
            .....
            .....
            #....
            #.#..
            #.#.#
            #####
        """.trimIndent()

class Day25Part1: BehaviorSpec() { init {

    Given("example") {
        val (locks, keys) = parseLocksAndKeys(exampleInputDay25)
        Then("locks and keys should be parsed") {
            locks.size shouldBe 2
            keys.size shouldBe 3
            locks[0] shouldBe listOf(0, 5, 3, 4, 3)
            keys[0] shouldBe listOf(5, 0, 2, 1, 3)
        }
        When("finding matching locks and keys") {
            val matchingLocksKeys = matchLocksAndKeys(locks, keys)
            Then("should have found matches") {
                matchingLocksKeys.size shouldBe 3
                matchingLocksKeys[0] shouldBe (listOf(0, 5, 3, 4, 3) to listOf(3, 0, 2, 0, 1))
            }
        }
    }

    Given("exercise input") {
        val (locks, keys) = parseLocksAndKeys(readResource("inputDay25.txt")!!)
        Then("should be parsed") {
            locks.size shouldBe 250
            keys.size shouldBe 250
        }
        When("finding matching locks and keys") {
            val matchingLocksKeys = matchLocksAndKeys(locks, keys)
            Then("should have found matches") {
                matchingLocksKeys.size shouldBe 3344
            }
        }
    }
}}

private fun parseLocksAndKeys(input: String): Pair<List<List<Int>>, List<List<Int>>> {
    fun transpose(block: List<List<Char>>) =
        (0 until block[0].size). map { x ->
            (0 until block.size).map { y ->
                block[y][x]
            }
        }
    fun toInts(block: List<List<Char>>) =
        transpose(block).map { row -> row.count { it == '#'} - 1}

    val locksChars = mutableListOf<List<List<Char>>>()
    val keysChars = mutableListOf<List<List<Char>>>()

    val blocksLines: List<List<String>> = input.split("\n\n").map { blocks ->
        blocks.split("\n")
    }
    for (blockLines in blocksLines) {
        if (blockLines.first().all { it == '#'}) {
            val lockChars = blockLines.map { line -> line.toCharArray().toList() }
            locksChars.add(lockChars)
        } else if (blockLines.last().all { it == '#'}) {
            val keyChars = blockLines.map { line -> line.toCharArray().toList() }
            keysChars.add(keyChars)
        }
    }
    return locksChars.map { toInts(it) } to keysChars.map { toInts(it) }
}

private fun matchLocksAndKeys(locks: List<List<Int>>, keys: List<List<Int>>) = sequence {
    for (lock in locks) {
        for (key in keys) {
            if (lock.zip(key).all { it.first + it.second < 6 }) yield (lock to key)
        }
    }
}.toList()
