import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

val exampleInputDay06 = """
        ....#.....
        .........#
        ..........
        ..#.......
        .......#..
        ..........
        .#..^.....
        ........#.
        #.........
        ......#...
        """.trimIndent()

class Day06Part1: BehaviorSpec() { init {

    Given("example input") {
        val situationMap = parseSituationMap(exampleInputDay06)
        Then("situation map should be parsed") {
            situationMap.size shouldBe 10
            situationMap[0].size shouldBe 10
        }
        When("searching the position of the guard") {
            val startPos = situationMap.findStartPosition('^')
            Then("start postion") {
                startPos shouldBe Coord2(4, 6)
            }
            When("fallow the guard") {
                val path = fallowTheGuard(situationMap, startPos)
                path.shouldNotBeNull()
                Then("fallow the guard should leave the map at the right position") {
                    path.last() shouldBe Coord2(7, 9)
                }
                Then("path should have the right length") {
                    path.toSet().size shouldBe 41
                }
                Then("map and path should look right") {
                    printSituationMapAndPath(situationMap, path) shouldBe
                            """
                                ....#.....
                                ....XXXXX#
                                ....X...X.
                                ..#.X...X.
                                ..XXXXX#X.
                                ..X.X.X.X.
                                .#XXXXXXX.
                                .XXXXXXX#.
                                #XXXXXXX..
                                ......#X..
                            """.trimIndent()
                }
            }
        }
    }

    Given("exercise input") {
        val situationMap = parseSituationMap(readResource("inputDay06.txt")!!)
        Then("situation map should be parsed") {
            situationMap.size shouldBe 130
            situationMap[0].size shouldBe 130
        }
        Then("should find solution") {
            val startPos = situationMap.findStartPosition('^')
            val path = fallowTheGuard(situationMap, startPos)
            path.shouldNotBeNull()
            //println("Leaving map at ${path.last()}")
            //println(printSituationMapAndPath(situationMap, path))
            path.toSet().size shouldBeGreaterThan 4972
            path.toSet().size shouldBe 4973
        }
    }
} }

class Day06Part2: BehaviorSpec() { init {

    Given("small example with a loop") {
        val situationMap = parseSituationMap("""
            ####
            #..#
            #^.#
            ####
        """.trimIndent())
        val startPos = situationMap.findStartPosition('^')
        Then("guard should run in a loop") {
            val path = fallowTheGuard(situationMap, startPos)
            path.shouldBeNull()
        }
    }
    Given("example input") {
        val situationMap = parseSituationMap(exampleInputDay06)
        val startPos = situationMap.findStartPosition('^')
        When("adding the right obstruction") {
            val changedSituationMap = situationMap.map { it.toMutableList() }
            changedSituationMap[6][3] = '#'
            Then("guard should run in a loop") {
                val path = fallowTheGuard(changedSituationMap, startPos)
                path.shouldBeNull()
            }
        }
        When("searching for all obstructions") {
            val obstrations = searchObstructions(situationMap, startPos)
            Then("should have found all") {
                obstrations.size shouldBe 6
            }
        }
    }

    xGiven("exercise input") { // Runs for about 5 seconds
        val situationMap = parseSituationMap(readResource("inputDay06.txt")!!)
        val startPos = situationMap.findStartPosition('^')
        Then("situation map should be parsed") {
            situationMap.size shouldBe 130
            situationMap[0].size shouldBe 130
        }
        Then("should find solution") {
            val obstrations = searchObstructions(situationMap, startPos)
            obstrations.size shouldBe 1482
        }
    }
} }

private enum class Direction(val coord: Coord2) {
    UP(Coord2(0, -1)),
    RIGHT(Coord2(1, 0)),
    DOWN(Coord2(0, 1)),
    LEFT(Coord2(-1, 0))
}

private fun fallowTheGuard(situationMap: List<List<Char>>, startPos: Coord2): List<Coord2>? {
    var currentPos = startPos
    var dir = Direction.UP
    val visited = mutableSetOf(currentPos to dir)
    val path = mutableListOf(currentPos)
    while(true) {
        val nextPos = currentPos + dir.coord
        if (! (nextPos.x in 0 until situationMap[0].size
            && nextPos.y in 0 until situationMap.size)) break
        if (situationMap[nextPos.y][nextPos.x] == '#') {
            dir = turn90(dir)
            if (visited.contains((currentPos to dir))) return null // Loop
        } else {
            currentPos = nextPos
            if (visited.contains(currentPos to dir)) return null // Loop
            visited.add(currentPos to dir)
            path.add(currentPos)
        }
    }
    return path
}

private fun searchObstructions(situationMap: List<List<Char>>, startPos: Coord2): List<Coord2> = sequence {
    for (x in situationMap[0].indices)
        for(y in situationMap.indices)
            if (situationMap[y][x] == '.') {
                val changedSituationMap = situationMap.map { it.toMutableList() }
                changedSituationMap[y][x] = '#'
                val path = fallowTheGuard(changedSituationMap, startPos)
                if (path == null) {
                    yield(Coord2(x, y))
                }
            }
}.toList()

private fun parseSituationMap(input: String): List<List<Char>> = input.split("\n").map { it.toList() }
private fun List<List<Char>>.findStartPosition(c: Char): Coord2 {
    for (x in this[0].indices)
        for(y in this.indices)
            if (this[y][x] == c) return Coord2(x, y)
    throw IllegalArgumentException("$c not found")
}

private fun turn90(dir: Direction) =
        when(dir) {
            Direction.UP -> Direction.RIGHT
            Direction.RIGHT -> Direction.DOWN
            Direction.DOWN -> Direction.LEFT
            Direction.LEFT -> Direction.UP
        }

private fun printSituationMapAndPath(situationMap: List<List<Char>>, path: List<Coord2>): String {
    val pathSet = path.map { it }.toSet()
    return situationMap.indices.map { y ->
        situationMap[0].indices.map { x ->
            if (pathSet.contains(Coord2(x, y))) 'X'
            else situationMap[y][x]
        }.joinToString("")
    }.joinToString("\n")

}
