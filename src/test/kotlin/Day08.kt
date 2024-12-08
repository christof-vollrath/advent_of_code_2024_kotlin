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

val exampleResultDay08 = """
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
                    antinodePairs shouldContain ((Coord2(x=2, y=3) to Coord2(x=11, y=0)))
                }
                val antinodeCoords = flattenAndFilterAntinodePairs(antinodePairs, size)
                Then("should have the right size") {
                    antinodeCoords.size shouldBe 14
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
        }    }
}

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
}

class Day08Part2: BehaviorSpec() { init {
    Given("example input") {
    }

    Given("exercise input") {
    }
} }

data class Antenna(val id: Char, val coord: Coord2)