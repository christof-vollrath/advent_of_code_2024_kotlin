import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class Day10Part1: BehaviorSpec() { init {

    Given("very simple example") {
        val map = parseIslandMap("""
                01
                32
            """.trimIndent()
        )
        Then("should been parsed") {
            map shouldBe listOf(
                listOf(0, 1),
                listOf(3, 2)
            )
        }
        When("finding trailheads") {
            val trailheads = findTrailheads(map)
            Then("should find one trailhead") {
                trailheads shouldBe listOf(Coord2(0, 0))
            }
            When("finding paths for trailhead") {
                val paths = findPathsForTrailhead(trailheads[0], map, 3)
                Then("should have found the one path") {
                    paths.size shouldBe 1
                    paths[0] shouldBe listOf(Coord2(0, 0), Coord2(1, 0), Coord2(1, 1), Coord2(0, 1))
                }
            }
            When("finding paths") {
                val paths = findPaths(map, 3)
                Then("should have found the one path") {
                    paths.size shouldBe 1
                    paths.first() shouldBe (Coord2(0, 0) to listOf(listOf(Coord2(0, 0), Coord2(1, 0), Coord2(1, 1), Coord2(0, 1))))
                }
            }
        }
    }
    Given("more examples") {
        val example1 = """
            0123
            1234
            8765
            9876            
        """.trimIndent()
        val example1Result = findPaths(parseIslandMap(example1), 9)
        Then("should have found path") {
            example1Result.size shouldBe 1
            example1Result[0].first shouldBe Coord2(0, 0)
            example1Result[0].second.size shouldBe 1
        }
        val example2 = """
            ...0...
            ...1...
            ...2...
            6543456
            7.....7
            8.....8
            9.....9      
        """.trimIndent()
        val example2Result = findPaths(parseIslandMap(example2), 9)
        Then("should have found path") {
            example2Result.size shouldBe 1
            example2Result[0].second.size shouldBe 2
        }
        val example3 = """
            10..9..
            2...8..
            3...7..
            4567654
            ...8..3
            ...9..2
            .....01
        """.trimIndent()
        val example3Result = findPaths(parseIslandMap(example3), 9)
        Then("should have found paths") {
            example3Result.size shouldBe 2
            example3Result[0].second.size shouldBe 1
            example3Result[1].second.size shouldBe 2
        }
        val example4 = """
            89010123
            78121874
            87430965
            96549874
            45678903
            32019012
            01329801
            10456732
        """.trimIndent()
        val example4Result = findPaths(parseIslandMap(example4), 9)
        Then("should have found paths") {
            example4Result.size shouldBe 9
            example4Result.map { it.second.size } shouldBe listOf(5, 6, 5, 3, 1, 3, 5, 3, 5)
            example4Result.map { it.second.size }.sum() shouldBe 36
        }
    }

    Given("exercise input") {
        val input = readResource("inputDay10.txt")!!
        val result = findPaths(parseIslandMap(input), 9)
        Then("result should have the right sum") {
            result.map { it.second.size }.sum() shouldBe 794
        }
    }
} }

class Day10Part2: BehaviorSpec() { init {

    Given("examples") {
        val example1 = """
            .....0.
            ..4321.
            ..5..2.
            ..6543.
            ..7..4.
            ..8765.
            ..9....           
        """.trimIndent()
        val example1Result = findPaths2(parseIslandMap(example1), 9)
        Then("should have found 3 trails") {
            example1Result.size shouldBe 1
            example1Result[0].first shouldBe Coord2(5, 0)
            example1Result[0].second.size shouldBe 3
        }
        val example2 = """
            ..90..9
            ...1.98
            ...2..7
            6543456
            765.987
            876....
            987....           
        """.trimIndent()
        val example2Result = findPaths2(parseIslandMap(example2), 9)
        Then("should have found trails") {
            example2Result.size shouldBe 1
            example2Result[0].second.size shouldBe 13
        }
        val example3 = """
            012345
            123456
            234567
            345678
            4.6789
            56789.          
        """.trimIndent()
        val example3Result = findPaths2(parseIslandMap(example3), 9)
        Then("should have found trails") {
            example3Result.map { it.second.size }.sum() shouldBe 227
        }
    }

    Given("exercise input") {
        val input = readResource("inputDay10.txt")!!
        val result = findPaths2(parseIslandMap(input), 9)
        Then("result should have the right sum") {
            result.map { it.second.size }.sum() shouldBe 1706
        }
    }
} }

private fun parseIslandMap(input: String): List<List<Int>> = input.split("\n"). map { line ->
    line.trim().map { c ->
        if (c == '.') -1
        else c.toString().toInt()
    }
}

private fun findTrailheads(map: List<List<Int>>): List<Coord2> = sequence {
    for (y in map.indices)
        for (x in map[y].indices) {
            if (map[y][x] == 0) yield(Coord2(x, y))
        }
}.toList()

private fun findPathsForTrailhead(trailhead: Coord2, map: List<List<Int>>, highest: Int): List<List<Coord2>> {
    val pathsFound = mutableListOf<List<Coord2>>()
    var currentPaths = listOf(listOf(trailhead))
    val currentVisited = mutableSetOf(trailhead)
    while(currentPaths.isNotEmpty()) {
        val nextPaths = mutableListOf<List<Coord2>>()
        currentPaths.forEachIndexed { i, currentPath ->
            val currentCoord = currentPath.last()
            val nextSteps = findNextSteps(currentCoord, map).filter { nextStep ->
                map[nextStep.y][nextStep.x] - map[currentCoord.y][currentCoord.x] == 1
            }
            for(nextStep in nextSteps) {
                if (map[nextStep.y][nextStep.x] == highest && ! currentVisited.contains(nextStep)) {
                    pathsFound.add(currentPath + nextStep) // reached an end
                    currentVisited.add(nextStep)
                } else if (! currentVisited.contains(nextStep)) { // continue paths
                    nextPaths.add(currentPath + nextStep)
                    currentVisited.add(nextStep)
                }
            }
        }
        currentPaths = nextPaths
    }
    return pathsFound
}

private fun findNextSteps(coord: Coord2, map: List<List<Int>>): List<Coord2> =
    listOf(Coord2(1, 0), Coord2(0, 1), Coord2(-1, 0), Coord2(0, -1)).map { delta ->
        coord + delta
    }.filter { step ->
        step.x >= 0 && step.x < map[0].size &&
        step.y >= 0 && step.y < map.size
    }

private fun findPaths(map: List<List<Int>>, highest: Int): List<Pair<Coord2, List<List<Coord2>>>> = findTrailheads(map).map { trailhead ->
    trailhead to findPathsForTrailhead(trailhead, map, highest)
}

private fun findPaths2(map: List<List<Int>>, highest: Int): List<Pair<Coord2, List<List<Coord2>>>> = findTrailheads(map).map { trailhead ->
    trailhead to findPathsForTrailhead2(trailhead, map, highest)
}
private fun findPathsForTrailhead2(trailhead: Coord2, map: List<List<Int>>, highest: Int): List<List<Coord2>> {
    val pathsFound = mutableListOf<List<Coord2>>()
    var currentPaths = listOf(listOf(trailhead))
    while(currentPaths.isNotEmpty()) {
        val nextPaths = mutableListOf<List<Coord2>>()
        currentPaths.forEachIndexed { i, currentPath ->
            val currentCoord = currentPath.last()
            val nextSteps = findNextSteps(currentCoord, map).filter { nextStep ->
                map[nextStep.y][nextStep.x] - map[currentCoord.y][currentCoord.x] == 1
            }
            for(nextStep in nextSteps) {
                if (map[nextStep.y][nextStep.x] == highest) {
                    pathsFound.add(currentPath + nextStep) // reached an end
                } else {
                    nextPaths.add(currentPath + nextStep)
                }
            } // Since climbing one step no danger to go back
        }
        currentPaths = nextPaths
    }
    return pathsFound
}
