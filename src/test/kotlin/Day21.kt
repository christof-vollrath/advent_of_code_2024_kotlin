import io.kotest.core.spec.style.BehaviorSpec

import io.kotest.matchers.shouldBe
import kotlin.math.max
import kotlin.math.min

val numericKeypadLayout = """
        +---+---+---+
        | 7 | 8 | 9 |
        +---+---+---+
        | 4 | 5 | 6 |
        +---+---+---+
        | 1 | 2 | 3 |
        +---+---+---+
        |   | 0 | A |
        +---+---+---+
""".trimIndent()

val directionalKeypadLayout = """    
        +---+---+
        | ^ | A |
    +---+---+---+
    | < | v | > |
    +---+---+---+
""".trimIndent()

val exampleInputDay21 = """
        029A
        980A
        179A
        456A
        379A
""".trimMargin()

val exerciseInputDay21 = """
        671A
        826A
        670A
        085A
        283A
""".trimMargin()


class Day21Part1: BehaviorSpec() { init {

    Given("numeric keypad and directional keypad") {
        val numericKeypad = parseKeypad(numericKeypadLayout)
        Then("numeric keypad should be parsed") {
            numericKeypad.size shouldBe 12
            numericKeypad['7'] shouldBe Coord2(0, 0)
            numericKeypad[' '] shouldBe Coord2(0, 3)
        }
        val directionalKeypad = parseKeypad(directionalKeypadLayout)
        Then("directional keypad should be parsed") {
            directionalKeypad.size shouldBe 6
            directionalKeypad[' '] shouldBe Coord2(0, 0)
            directionalKeypad['v'] shouldBe Coord2(1, 1)
        }
        When("finding one shortest path for numeric keypad") {
            val paths = findKeypadPaths(numericKeypad, "029A")
            paths shouldBe setOf(
                "<A^A>^^AvvvA",
                "<A^A^^>AvvvA"
            )
        }
        When("finding one shortest path for numeric keypad when empty button might be passed") {
            val paths = findKeypadPaths(numericKeypad, "4")
            paths shouldBe setOf(
                "^^<<A"
            )
        }
        Then("find one shortest sequence for one code") {
            val paths = findSequencesForChainedKeypads(numericKeypad, directionalKeypad, "029A")
            println(paths.minBy { it.last().length } )
            paths.map { it.last() }.minBy { it.length }.length shouldBe 68
        }
        Then("finding one shortest sequence for another code") {
            val paths = findSequencesForChainedKeypads(numericKeypad, directionalKeypad, "456A")
            println(paths.minBy { it.last().length })
            val lastSequence = paths.minBy { it.last().length }.last()
            lastSequence.length shouldBe 64
            val lastSequence2 = findShortesSequenceForChainedKeypads(numericKeypad, directionalKeypad, listOf("456A")).first().second
            lastSequence2 shouldBe lastSequence
        }
        When("searching shortest sequence for all codes") {
            val sequences = findShortesSequenceForChainedKeypads(numericKeypad, directionalKeypad, exampleInputDay21.split("\n").map { it.trim() })
            Then("should have found sequences") {
                sequences.map { (code, sequence) -> code to sequence.length } shouldBe
                        parseCodesWithSequences("""
                        029A: <vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
                        980A: <v<A>>^AAAvA^A<vA<AA>>^AvAA<^A>A<v<A>A>^AAAvA<^A>A<vA>^A<A>A
                        179A: <v<A>>^A<vA<A>>^AAvAA<^A>A<v<A>>^AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
                        456A: <v<A>>^AA<vA<A>>^AAvAA<^A>A<vA>^A<A>A<vA>^A<A>A<v<A>A>^AAvA<^A>A
                        379A: <v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
                    """.trimIndent()).map { (code, sequence) -> code to sequence.length }
            }
            Then("should calculate complexity") {
                val complexities = calculateComplexities(sequences)
                complexities shouldBe listOf(68 * 29, 60 * 980, 68 * 179, 64 * 456, 64 * 379)
                complexities.sum() shouldBe 126384
            }
        }
    }

    Given("exercise input, numeric and directional keypad") {
        val numericKeypad = parseKeypad(numericKeypadLayout)
        val directionalKeypad = parseKeypad(directionalKeypadLayout)
        When("searching shortest sequence for all codes") {
            val sequences = findShortesSequenceForChainedKeypads(numericKeypad, directionalKeypad, exerciseInputDay21.split("\n").map { it.trim() })
            Then("should calculate complexity") {
                val complexities = calculateComplexities(sequences)
                complexities.sum() shouldBe 182844
            }
        }
    }
}}


private fun parseKeypad(input: String) = sequence {
    val lines = input.split("\n")
    for (y in 0 until lines.count())
        for (x in 0 until lines[y].length)
            if ((y - 1) % 2 == 0 && (x-2) % 4 == 0) // skip  borders
                yield(lines[y][x] to Coord2((x  - 2)/ 4, (y - 1) / 2))
}.toMap()

private fun findKeypadPaths(keypad: Map<Char, Coord2>, path: String): Set<String> {
    fun findXPath(from: Int, to: Int) = when {
        to - from < 0 -> "<".repeat(from - to)
        to - from > 0 -> ">".repeat(to - from)
        else -> ""
    }
    fun findYPath(from: Int, to: Int) = when {
        to - from < 0 -> "^".repeat(from - to)
        to - from > 0 -> "v".repeat(to - from)
        else -> ""
    }
    fun find2Paths(from: Coord2, to: Coord2, emptyButton: Coord2?) = sequence { // Ignoring Zigzag path because they are not optimal for directional keypad
        if (emptyButton != null) { // make sure not crossing empty button
            // horizontal than vertical move
            if (! (emptyButton.y == from.y && emptyButton.x in min(from.x, to.x) .. max(from.x, to.x)))
                yield(findXPath(from.x, to.x) + findYPath(from.y, to.y))
            // vertical than horizontal move
            if (! (emptyButton.x == from.x && emptyButton.y in min(from.y, to.y) .. max(from.y, to.y)))
                yield(findYPath(from.y, to.y) + findXPath(from.x, to.x))
        } else {
            yield(findXPath(from.x, to.x) + findYPath(from.y, to.y))
            yield(findYPath(from.y, to.y) + findXPath(from.x, to.x))
        }
    }.toList()
    var currCoord = keypad['A'] ?: throw IllegalArgumentException("Button A not found on pad")
    var currPaths = setOf<String>("")
    val emptyButton = keypad[' ']
    for(button in path) {
        val nextCoord = keypad[button] ?: throw IllegalArgumentException("Button $button not found on pad")
        val nextPaths = currPaths.flatMap { currPath ->
            val paths = find2Paths(currCoord, nextCoord, emptyButton).toSet()
            paths.map { path ->
                currPath + path + 'A'
            }
        }.toSet()
        currCoord = nextCoord
        currPaths = nextPaths
    }
    return currPaths
}

private fun findSequencesForChainedKeypads(numericalKeypad: Map<Char, Coord2>, directionalKeypd: Map<Char, Coord2>, startPath:String): Set<List<String>> {
    val paths1 = findKeypadPaths(numericalKeypad, startPath).map { listOf(startPath, it) }.toSet()
    val paths2 = paths1.flatMap { path1 ->
        findKeypadPaths(directionalKeypd, path1.last()).map { path1 + it }
    }.toSet()
    val paths3 = paths2.flatMap { path2 ->
        findKeypadPaths(directionalKeypd, path2.last()).map { path2 + it }
    }.toSet()
    return paths3
}

private fun findShortesSequenceForChainedKeypads(numericalKeypad: Map<Char, Coord2>, directionalKeypd: Map<Char, Coord2>, codes: List<String>): List<Pair<String, String>> =
    codes.map { code ->
        //val sequences = findSequencesForChainedKeypads(numericalKeypad, directionalKeypd, code).map { solutionForCode -> solutionForCode.last() }
        val solution = findSequencesForChainedKeypads(numericalKeypad, directionalKeypd, code)
        val finalSequences = solution.map { it.last() }
        code to finalSequences.minBy { it.length }
    }

private fun parseCodesWithSequences(input: String) = input.split("\n").map { line ->
    val regex = """([\dA]+): ([<>^vA]+)""".toRegex()
    val (code, sequence) = regex
        .matchEntire(line)
        ?.destructured
        ?.toList()
        ?: throw IllegalArgumentException("could not parse buttons in $line")
    code to sequence
}

private fun calculateComplexities(solutions: List<Pair<String, String>>): List<Int> =
    solutions.map { calculateComplexity(it.first, it.second) }

private fun calculateComplexity(code: String, sequence: String) =
    code.filter { it.isDigit() }.toInt() * sequence.length
