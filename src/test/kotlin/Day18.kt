import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInputDay18 = """
            5,4
            4,2
            4,5
            3,0
            2,1
            6,3
            2,4
            1,5
            0,6
            3,3
            2,6
            5,1
            1,2
            5,5
            2,5
            6,5
            1,4
            0,4
            6,4
            1,1
            6,1
            1,0
            0,5
            1,6
            2,0
        """.trimIndent()


class Day18Part1: BehaviorSpec() { init {

    Given("example") {
        val byteCoords = parseIncomingBytes(exampleInputDay18)
        Then("should be parsed") {
            byteCoords.size shouldBe 25
            byteCoords[2] shouldBe Coord2(4, 5)
        }
        When("creating memory") {
            val first12Bytes = byteCoords.take(12)
            val memory = createMemory(Coord2(6, 6), first12Bytes)
            Then("should be printable") {
                printMemory(memory) shouldBe """
                ...#...
                ..#..#.
                ....#..
                ...#..#
                ..#..#.
                .#..#..
                #.#....
            """.trimIndent()
            }
            Then("should be printable with path") {
                printMemory(memory, listOf(Coord2(0, 0), Coord2(1, 0))) shouldBe
                        """
                            OO.#...
                            ..#..#.
                            ....#..
                            ...#..#
                            ..#..#.
                            .#..#..
                            #.#....
                        """.trimIndent()
            }
            When("finding path") {
                val path = findMemoryPath(Coord2(0, 0), Coord2(6, 6), memory)
                Then("path should be right") {
                    printMemory(memory, path) shouldBe
                            """
                                OO.#OOO
                                .O#OO#O
                                .OOO#OO
                                ...#OO#
                                ..#OO#.
                                .#.O#..
                                #.#OOOO
                            """.trimIndent()
                    path.size-1 shouldBe 22 // steps not including the start
                }
            }
        }
    }
    Given("exercise input") {
        val byteCoords = parseIncomingBytes(readResource("inputDay18.txt")!!)
        Then("should be parsed") {
            byteCoords.size shouldBe 3450
        }
        When("initialising memory") {
            val firstKilobyte = byteCoords.take(1024)
            val memory = createMemory(Coord2(70, 70), firstKilobyte)
            val path = findMemoryPath(Coord2(0, 0), Coord2(70, 70), memory)
            //println(printMemory(memory, path))
            Then("should have found the solution") {
                val solution = path.size - 1
                solution shouldBe 372
            }

        }
    }
} }

class Day18Part2: BehaviorSpec() { init {

    Given("example") {
        val byteCoords = parseIncomingBytes(exampleInputDay18)
        val memory = createMemory(Coord2(6, 6))
        When("finding blocking memory") {
            val block = findBlockingMemoryPath(Coord2(0, 0), Coord2(6, 6), memory, byteCoords)
            Then("should find solution") {
                block shouldBe Coord2(6, 1)
            }
        }
    }

    Given("exercise input") {
        val byteCoords = parseIncomingBytes(readResource("inputDay18.txt")!!)
        val memory = createMemory(Coord2(70, 70))
        When("finding blocking memory") {
            val block = findBlockingMemoryPath(Coord2(0, 0), Coord2(70, 70), memory, byteCoords)
            Then("should find solution") {
                block shouldBe Coord2(25, 6)
            }
        }
    }
} }

private fun parseIncomingBytes(input: String) = input.split("\n").map { line ->
    val regex = """(\d+),(\d+)""".toRegex()
    val nums = regex
        .matchEntire(line)
        ?.destructured
        ?.toList()
        ?.map { it.toInt() }
        ?: throw IllegalArgumentException("could not parse byte position $line")
    Coord2(nums[0], nums[1])
}

private fun createMemory(lowerRightCorner: Coord2, bytePositions: List<Coord2> = emptyList()): List<MutableList<Char>> {
    val result = (0 .. lowerRightCorner.y).map { y ->
        (0 .. lowerRightCorner.x).map { x ->
            '.'
        }.toMutableList()
    }
    bytePositions.forEach { p ->
        result[p.y][p.x] = '#'
    }
    return result
}

private fun printMemory(memory: List<List<Char>>, path: List<Coord2> = emptyList()): String {
    val pathSet = path.toSet()
    return memory.mapIndexed { y, line ->
        line.mapIndexed { x, c ->
          if (pathSet.contains(Coord2(x, y))) 'O'
          else memory[y][x]
        }.joinToString("")
    }.joinToString("\n")
}

private fun findMemoryPath(from: Coord2, to: Coord2, memory: List<List<Char>>): List<Coord2> {
    var currPaths = listOf(listOf(from))
    val visited = mutableSetOf(from)
    while(true) {
        val nextPaths = mutableListOf<List<Coord2>>()
        currPaths.forEach { currPath ->
            if (currPath.last() == to) return currPath // found!
            val next = nextMemories(currPath.last(), Coord2(memory[0].size-1, memory.size-1))
            val notYetVisited = next.filter { ! visited.contains(it) }
            val notBlocked = notYetVisited.filter { memory[it.y][it.x] != '#'}
            notBlocked.forEach {
                val nextPath = currPath + it
                nextPaths.add(nextPath)
                visited.add(it)
            }
        }
        if (nextPaths.isEmpty()) return emptyList()
        currPaths = nextPaths
    }
}

val cornerDeltas = listOf(Coord2(1, 0), Coord2(0, 1), Coord2(-1, 0), Coord2(0, -1))

private fun nextMemories(pos: Coord2, lowerRightCorner: Coord2) = cornerDeltas.map { pos + it }
    .filter { 0 <= it.x && 0 <= it.y && it.x <= lowerRightCorner.x && it.y <= lowerRightCorner.y }

private fun findBlockingMemoryPath(from: Coord2, to: Coord2, memory: List<MutableList<Char>>, byteCoords: List<Coord2>): Coord2 {
    var path: List<Coord2> = emptyList()
    byteCoords.forEach { coord ->
        memory[coord.y][coord.x] = '#'
        path = findMemoryPath(from, to, memory)
        if (path.isEmpty()) return coord
    }
    throw IllegalArgumentException("path found even with all bytes blocked:\n ${printMemory(memory, path)}")
}