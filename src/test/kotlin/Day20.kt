import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan

import io.kotest.matchers.shouldBe

import kotlin.IllegalStateException

val exampleInputDay20 = """
            ###############
            #...#...#.....#
            #.#.#.#.#.###.#
            #S#...#.#.#...#
            #######.#.#.###
            #######.#.#...#
            #######.#.###.#
            ###..E#...#...#
            ###.#######.###
            #...###...#...#
            #.#####.#.###.#
            #.#...#.#.#...#
            #.#.#.#.#.#.###
            #...#...#...###
            ###############
        """.trimIndent()


class Day20Part1: BehaviorSpec() { init {

    Given("example") {
        val (start, end, maze) = parseRacetrack(exampleInputDay20)
        Then("should be parsed") {
            maze.size shouldBe 15
            maze[0].size shouldBe 15
            start shouldBe Coord2(1, 3)
            end shouldBe Coord2(5, 7)
        }
        When("path should be found") {
            val path = findMazePath(start, end, maze)
            Then("path should be found") {
                path.size - 1 shouldBe 84 // path contains start
                path.last().second shouldBe 84
            }
            When("finding shortcuts") {
                val shortCuts = findShortCuts(path)
                Then("shortcuts should be found") {
                    shortCuts.size shouldBe 44
                }
                Then("a more flexible version should find the same results") {
                    val shortCuts2 = findShortCuts(path, 2)
                    shortCuts2.toSet() shouldBe shortCuts.toSet()
                }
                Then("shortcuts can be grouped") {
                    val groupedAndSorted = groupAndSortShortCuts(shortCuts)
                    groupedAndSorted[0].first shouldBe 2
                    groupedAndSorted[0].second.size shouldBe 14
                }
            }
        }
    }

    Given("exercise input") {
        val (start, end, maze) = parseRacetrack(readResource("inputDay20.txt")!!)
        Then("should be parsed") {
            maze.size shouldBe 141
        }
        When("path should be found") {
            val path = findMazePath(start, end, maze)
            Then("path should be found") {
                path.size - 1 shouldBe 9336 // path contains start
                path.last().second shouldBe 9336
            }
            When("finding shortcuts") {
                val shortCuts = findShortCuts(path)
                Then("should be found") {
                    shortCuts.size shouldBe 6948
                    val groupedAndSorted100 = groupAndSortShortCuts(shortCuts)
                        .filter { it.first >= 100}
                    val sumCount = groupedAndSorted100.sumOf { it.second.size }
                    sumCount shouldBe 1372
                }
                Then("a more flexible version should find the same results") {
                    val shortCuts2 = findShortCuts(path, 2)
                    shortCuts2.size shouldBe 6948 // unfortunately shortcuts are found in a different order
                    shortCuts2.toSet() shouldBe shortCuts.toSet()
                }
                Then("more flexible version with limit for savings") {
                    val shortCuts2 = findShortCuts(path, 2, 100)
                    val groupedAndSorted = groupAndSortShortCuts(shortCuts2)
                    groupedAndSorted.sumOf { it.second.size } shouldBe 1372
                }
            }
        }
    }
}}

class Day20Part2: BehaviorSpec() { init {

    Given("example") {
        val (start, end, maze) = parseRacetrack(exampleInputDay20)
        When("path should be found") {
            val path = findMazePath(start, end, maze)
            Then("path should be found") {
                path.last().second shouldBe 84
            }
            When("finding shortcuts") {
                val shortCuts = findShortCuts(path, 20, 50)
                Then("shortcuts should be found") {
                    shortCuts.size shouldBeGreaterThan 44
                }
                Then("shortcuts can be grouped") {
                    val groupedAndSorted = groupAndSortShortCuts(shortCuts)
                    groupedAndSorted.map { (savings, shortCuts) -> shortCuts.size to savings } shouldBe listOf (
                        32 to 50,
                        31 to 52,
                        29 to 54,
                        39 to 56,
                        25 to 58,
                        23 to 60,
                        20 to 62,
                        19 to 64,
                        12 to 66,
                        14 to 68,
                        12 to 70,
                        22 to 72,
                        4 to 74,
                        3 to 76
                    )
                }
            }
        }
    }

    Given("exercise input") {
        val (start, end, maze) = parseRacetrack(readResource("inputDay20.txt")!!)
        Then("should be parsed") {
            maze.size shouldBe 141
        }
        When("path should be found") {
            val path = findMazePath(start, end, maze)
            Then("path should be found") {
                path.last().second shouldBe 9336
            }
            When("finding shortcuts") {
                val shortCuts = findShortCuts(path, 20, 100)
                Then("shortcuts should be found") {
                    shortCuts.size shouldBe 979014
                }
            }
        }
    }
}}

private fun parseRacetrack(input: String): Triple<Coord2, Coord2, List<List<Char>>> {
    fun findChar(maze: List<List<Char>>, c: Char): Coord2 {
        for (y in 0 until maze.size)
            for (x in 0 until maze[y].size)
                if (maze[y][x] == c) return Coord2(x, y)
        throw IllegalArgumentException("Maze doesn't contain $c")
    }

    val maze = input.split("\n").map { line ->
        line.toCharArray().toList()
    }
    return Triple(findChar(maze, 'S'), findChar(maze, 'E'), maze)
}

private fun findMazePath(start: Coord2, end: Coord2, maze: List<List<Char>>): List<Pair<Coord2, Int>> {
    var currPaths = listOf(listOf(start to 0))
    val visited = mutableSetOf<Coord2>()
    while(true) {
        val nextPaths = mutableListOf<List<Pair<Coord2, Int>>>()
        for(currPath in currPaths) {
            if (currPath.last().first == end) return currPath // found! and there's only one solution
            val nextCoords = cornerDeltas.map { currPath.last().first + it }
                .filter { maze[it.y][it.x] != '#'}
                .filter { ! visited.contains(it) }
            for (nextCoord in nextCoords) {
                val nextPathEntry = nextCoord to (currPath.last().second + 1)
                nextPaths.add(currPath + nextPathEntry)
                visited.add(nextCoord)
            }
        }
        if (nextPaths.isEmpty()) throw IllegalStateException("No path found from $start to $end")
        currPaths = nextPaths
    }
}

private fun findShortCuts(path: List<Pair<Coord2, Int>>): List<Pair<Coord2, Int>> = sequence {
    val pathMap = path.toMap()
    for ((tile, currLength) in path) {
        cornerDeltas.forEach {
            val shortCutCoord = tile + it
            val shortCutEndCoord = shortCutCoord + it // go two steps
            val shortCutTarget = pathMap[shortCutEndCoord]
            if (shortCutTarget != null) {
                val savings = shortCutTarget - currLength - 2
                if (savings > 1) yield(tile to savings)
            }
        }
    }
}.toList()

private fun findShortCuts(path: List<Pair<Coord2, Int>>, maxShortCutLen: Int, minSavings: Int = 1): List<Pair<Coord2, Int>> = sequence {

    path.forEachIndexed { i1, (currTile, currLength) ->
        for (i2 in i1 + 1 until path.size) {
            val shortCutCoord = path[i2].first
            val remainingLen = path[i2].second
            val shortCutLen = currTile.manhattanDistance(shortCutCoord)
            val savings = remainingLen - currLength - shortCutLen
            if (savings >= minSavings && shortCutLen <= maxShortCutLen)
                yield(currTile to savings)
        }
    }
}.toList()

private fun groupAndSortShortCuts(shortCuts: List<Pair<Coord2, Int>>): List<Pair<Int, List<Coord2>>> =
    shortCuts.sortedBy { it.second }
        .groupBy({ it.second }){ it.first }
        .toList()

private val cornerDeltas = listOf(Coord2(1, 0), Coord2(0, 1), Coord2(-1, 0), Coord2(0, -1))
