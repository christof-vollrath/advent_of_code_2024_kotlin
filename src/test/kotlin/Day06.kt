import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
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
            //println(printSituationMapAndPath(situationMap, path))
            path.toSet().size shouldBeGreaterThan 4972
            path.toSet().size shouldBe 4973
        }
    }
} }

private enum class Direction(val coord: Coord2) {
    UP(Coord2(0, -1)),
    RIGHT(Coord2(1, 0)),
    DOWN(Coord2(0, 1)),
    LEFT(Coord2(-1, 0))
}

private fun fallowTheGuard(situationMap: List<List<Char>>, startPos: Coord2): List<Coord2> = sequence {
    yield(startPos)
    var currentPos = startPos
    var dir = Direction.UP
    while(true) {
        val nextPos = currentPos + dir.coord
        if (! (nextPos.x in 0 until situationMap[0].size
            && nextPos.y in 0 until situationMap.size)) break
        if (situationMap[nextPos.y][nextPos.x] == '#') {
            dir = turn90(dir)
        } else {
            currentPos = nextPos
            yield(nextPos)
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
    val pathSet = path.toSet()
    return situationMap.indices.map { y ->
        situationMap[0].indices.map { x ->
            if (pathSet.contains(Coord2(x, y))) 'X'
            else situationMap[y][x]
        }.joinToString("")
    }.joinToString("\n")

}
