import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val exampleInputDay08 = """
            ............
            ........0...
            .....0......
            .......0....
            ....0.......
            ......A.....
            ............
            ............
            ........A...
            .........A..
            ............
            ............
        """.trimIndent()

val exampleResultDay08Part1 = """
            ......#....#
            ...#....0...
            ....#0....#.
            ..#....0....
            ....0....#..
            .#....A.....
            ...#........
            #......#....
            ........A...
            .........A..
            ..........#.
            ..........#.
        """.trimIndent()

val exampleResultDay08Part2 = """
            ##....#....#
            .#.#....0...
            ..#.#0....#.
            ..##...0....
            ....0....#..
            .#...#A....#
            ...#..#.....
            #....#.#....
            ..#.....A...
            ....#....A..
            .#........#.
            ...#......##
        """.trimIndent()

class Day08Part1: BehaviorSpec() { init {

    Given("find coords for two antennas") {
        val result = findAntinodeCoords(Coord2(8, 8) to Coord2(10, 10))
        Then("antinodes should be found") {
            result shouldBe (Coord2(12, 12) to Coord2(6, 6))
        }
    }
    Given("example input") {
        val (size, antennas) = parseAntennas(exampleInputDay08)
        Then("should be parsed correctly") {
            size shouldBe Coord2(12, 12)
            antennas.size shouldBe 7
            antennas shouldContain Antenna('0', Coord2(8, 1))
        }
        When("combining pairs of stations") {
            val pairs = combinePairsOfAntennas(antennas)
            Then("should be") {
                pairs.size shouldBe 3 + 2 + 1 + 2 + 1
                pairs shouldContain (Antenna('0', Coord2(8, 1)) to Antenna('0', Coord2(5, 2)))
            }
            When("finding antinodes") {
                val antinodePairs = findAntinodePairs(pairs)
                Then("should have found antinodes") {
                    antinodePairs.size shouldBe 9
                    antinodePairs shouldContain ((Coord2(x = 2, y = 3) to Coord2(x = 11, y = 0)))
                }
                val antinodeCoords = flattenAndFilterAntinodePairs(antinodePairs, size)
                Then("should have the right size") {
                    antinodeCoords.size shouldBe 14
                    printAntennasAndAntinodes(antennas, size, antinodeCoords) shouldBe exampleResultDay08Part1
                }
            }
        }
    }

    Given("exercise input") {
        val (size, antennas) = parseAntennas(readResource("inputDay08.txt")!!)
        val pairs = combinePairsOfAntennas(antennas)
        val antinodePairs = findAntinodePairs(pairs)
        val antinodeCoords = flattenAndFilterAntinodePairs(antinodePairs, size)
        Then("should have the right size") {
            antinodeCoords.size shouldBe 244
        }
    }
} }

class Day08Part2: BehaviorSpec() { init {

    Given("find coords for two antennas") {
        val result = findAntinodeCoords2(Coord2(0, 0) to Coord2(3, 1), Coord2(10, 10))
        Then("antinodes should be found") {
            result shouldBe setOf(Coord2(x=3, y=1), Coord2(x=6, y=2), Coord2(x=9, y=3), Coord2(x=0, y=0)) // Antennas must be included as resonant harmonic
        }
    }
    Given("another example") {
        val exampleWithAntinodes = """
            T....#....
            ...T......
            .T....#...
            .........#
            ..#.......
            ..........
            ...#......
            ..........
            ....#.....
            ..........
        """.trimIndent()
        val (size, antennas) = parseAntennas(exampleWithAntinodes.replace("#", "."))
        val pairs = combinePairsOfAntennas(antennas)
        val antinodeCoords = findAntinodes2(pairs, size)
        Then("should find result") {
            printAntennasAndAntinodes(antennas, size, antinodeCoords) shouldBe exampleWithAntinodes
            antinodeCoords.size shouldBe 9
        }
    }
    Given("example input") {
        val (size, antennas) = parseAntennas(exampleInputDay08)
        When("combining pairs of stations") {
            val pairs = combinePairsOfAntennas(antennas)
            val antinodeCoords = findAntinodes2(pairs, size)
            Then("should have found antinodes") {
                //println(printAntennasAndAntinodes(antennas, size, antinodeCoords))
                antinodeCoords.size shouldBe 34
                printAntennasAndAntinodes(antennas, size, antinodeCoords) shouldBe exampleResultDay08Part2
            }
        }

    }

    Given("exercise input") {
        val (size, antennas) = parseAntennas(readResource("inputDay08.txt")!!)
        val pairs = combinePairsOfAntennas(antennas)
        val antinodeCoords = findAntinodes2(pairs, size)
        Then("should have the right size") {
            antinodeCoords.size shouldBe 912
        }
    }
} }


data class Antenna(val id: Char, val coord: Coord2)

private fun flattenAndFilterAntinodePairs(antinodePairs: Set<Pair<Coord2, Coord2>>, size: Coord2): Set<Coord2> =
    antinodePairs.flatMap { antinodePair ->
        antinodePair.toList()
    }.filter { coord ->
        0 <= coord.x && coord.x < size.x && 0 <= coord.y && coord.y < size.y
    }.toSet()

private fun combinePairsOfAntennas(antennas: Set<Antenna>): Set<Pair<Antenna, Antenna>> {
    val groupedStations = antennas.groupBy { it.id }
    return groupedStations.flatMap { (_, antennas) ->
        sequence {
            var current = antennas.first()
            var next = antennas.drop(1)
            while (next.isNotEmpty()) {
                next.forEach { other ->
                    yield(current to other)
                }
                current = next.first()
                next = next.drop(1)
            }
        }.toSet()
    }.toSet()
}

private fun parseAntennas(input: String): Pair<Coord2, Set<Antenna>> {
    val lines = input.split("\n")
    val size = Coord2(lines[0].length, lines.size)
    val antennas = sequence {
        lines.mapIndexed{ y, line ->
            line.mapIndexed { x, id->
                if (id != '.') yield(Antenna(id, Coord2(x, y)))
            }
        }
    }.toSet()
    return size to antennas
}

private fun findAntinodePairs(antennaPairs: Set<Pair<Antenna, Antenna>>): Set<Pair<Coord2, Coord2>> =
    antennaPairs.map { findAntinodePair(it)}.toSet()

private fun findAntinodePair(antennaPair: Pair<Antenna, Antenna>): Pair<Coord2, Coord2> =
    findAntinodeCoords(antennaPair.first.coord to antennaPair.second.coord)

private fun findAntinodeCoords(antennaCoords: Pair<Coord2, Coord2>): Pair<Coord2, Coord2> =
    antennaCoords.second + (antennaCoords.second - antennaCoords.first) to antennaCoords.first + (antennaCoords.first - antennaCoords.second)

private fun printAntennasAndAntinodes(antennas: Set<Antenna>, size: Coord2, antinodes: Set<Coord2>): String {
    val antennasByCoords = antennas.map { it.coord to it }.toMap()
    return (0 until size.y).map { y ->
        (0 until size.x).map { x ->
            val currentCoord = Coord2(x, y)
            val antenna = antennasByCoords[currentCoord]
            if (antenna != null) antenna.id
            else if (antinodes.contains(currentCoord)) '#'
            else '.'
        }.joinToString("")
    }.joinToString("\n")
}

private fun findAntinodes2(antennaPairs: Set<Pair<Antenna, Antenna>>, size: Coord2): Set<Coord2> =
    antennaPairs.flatMap { findAntinodes2(it, size)}.toSet()

private fun findAntinodes2(antennaPair: Pair<Antenna, Antenna>, size: Coord2): Set<Coord2> =
    findAntinodeCoords2(antennaPair.first.coord to antennaPair.second.coord, size)

private fun findAntinodeCoords2(antennaCoords: Pair<Coord2, Coord2>, size: Coord2): Set<Coord2> = sequence {
    val delta = antennaCoords.second - antennaCoords.first
    var curr = antennaCoords.second
    while (curr.x >= 0 && curr.y >= 0 && curr.x < size.x && curr.y < size.y) {
        yield(curr)
        curr += delta
    }
    curr = antennaCoords.first
    while (curr.x >= 0 && curr.y >= 0 && curr.x < size.x && curr.y < size.y) {
        yield(curr)
        curr -= delta
    }

}.toSet()
