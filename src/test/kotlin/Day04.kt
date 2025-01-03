import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val exampleInputDay04String = """
        ..X...
        .SAMX.
        .A..A.
        XMAS.S
        .X....
    """.trimIndent()

val longerExampleDay04String = """
            MMMSXXMASM
            MSAMXMSMSA
            AMXSXMAAMM
            MSAMASMSMX
            XMASAMXAMM
            XXAMMXXAMA
            SMSMSASXSS
            SAXAMASAAA
            MAMMMXMMMM
            MXMXAXMASX
        """.trimIndent()

class Day04Part1: BehaviorSpec() { init {

    Given("example input") {
        val exampleInputDay04 = exampleInputDay04String.split("\n")
        When("getting lines") {
            val lines = exampleInputDay04.lines()
            Then("should have 5 lines and the correct first one") {
                lines.size shouldBe 5
                lines[0] shouldBe "..X..."
            }
        }
        When("getting rows") {
            val rows = exampleInputDay04.rows()
            Then("should have 6 rows and the correct first one") {
                rows.size shouldBe 6
                rows[0] shouldBe "...X."
            }
        }
        When("getting diagonals") {
            val diagonals1 = exampleInputDay04.diagonals1()
            Then("should have 10 diagonals and the correct samples") {
                diagonals1.size shouldBe 10
                diagonals1[1] shouldBe "XX"
                diagonals1[6] shouldBe "XMAS"
            }
            val diagonals2 = exampleInputDay04.diagonals2()
            Then("should have 10 diagonals and the correct samples") {
                println(diagonals2)
                diagonals2.size shouldBe 10
                diagonals2[1] shouldBe ".."
                diagonals2[6] shouldBe ".SA."
            }
        }
        When("getting all lines") {
            val allLines = exampleInputDay04.allLines()
            Then("should have 62 lines and the correct samples") {
                allLines.size shouldBe 62
                allLines[0] shouldBe "..X..."
                allLines[12] shouldBe "XX"
                allLines[17] shouldBe "XMAS"
                allLines[31] shouldBe "...X.."
                allLines[48] shouldBe "SAMX"
            }
            Then("count XMAS in lines") {
                allLines.countString("XMAS".toRegex()) shouldBe 4
            }
        }
    }
    Given("a longer example") {
        Then("count XMAS in lines") {
            longerExampleDay04String.split("\n").allLines().countString("XMAS".toRegex()) shouldBe 18
        }
    }

    Given("exercise input") {
        val inputLines = readResource("inputDay04.txt")!!.split("\n")
        Then("should should count XMAS") {
            inputLines.allLines().countString("XMAS".toRegex()) shouldBe 2662
        }
    }
} }


class Day04Part2: BehaviorSpec() { init {
    val xMas = """
        M.S
        .A.
        M.S
    """.trimIndent().split("\n")
    val xMasShapes = listOf(
        """
        M.S
        .A.
        M.S
    """.trimIndent().split("\n"),       """
        M.M
        .A.
        S.S
    """.trimIndent().split("\n"),
        """
        S.M
        .A.
        S.M
    """.trimIndent().split("\n"),
        """
        S.S
        .A.
        M.M
    """.trimIndent().split("\n")
    )
    Given("longer example input") {
        val longerExample = longerExampleDay04String.split("\n")
        Then("should find shape") {
            val shapes = longerExample.findShape(xMas)
            shapes shouldContain (1 to 0)
            shapes.size shouldBe 2
        }
        Then("should find shapes") {
            val shapes = longerExample.findShapes(xMasShapes)
            shapes shouldContain (1 to 0)
            shapes.size shouldBe 9
        }
    }

    Given("exercise input") {
        val inputLines = readResource("inputDay04.txt")!!.split("\n")
        Then("should should count X-MAS shapes") {
            val shapes = inputLines.findShapes(xMasShapes)
            shapes.size shouldBe 2034
        }
    }} }

fun WordPuzzle.lines() = this
fun WordPuzzle.rows() = (0 until this[0].length).map { x ->
    indices.map { y ->
        this[y][x] }.joinToString("")
}
fun WordPuzzle.diagonals1() = sequence {
    for (y in this@diagonals1.size-1 downTo 0) {
        val diagonal = sequence {
            var hx = 0; var hy = y
            while( hx < this@diagonals1[0].length && hy < this@diagonals1.size) {
                yield(this@diagonals1[hy][hx])
                hy++; hx++
            }
        }.joinToString("")
        yield(diagonal)
    }
    for (x in 1 until this@diagonals1[0].length) {
        val diagonal = sequence {
            var hx = x; var hy = 0
            while( hx < this@diagonals1[0].length && hy < this@diagonals1.size) {
                yield(this@diagonals1[hy][hx])
                hy++; hx++
            }
        }.joinToString("")
        yield(diagonal)
    }
}.toList()
fun WordPuzzle.diagonals2() = sequence {
    for (y in 0 until this@diagonals2.size-1) {
        val diagonal = sequence {
            var hx = 0; var hy = y
            while( hx < this@diagonals2[0].length && hy >= 0) {
                yield(this@diagonals2[hy][hx])
                hy--; hx++
            }
        }.joinToString("")
        yield(diagonal)
    }
    for (x in 0 until this@diagonals2[0].length) {
        val diagonal = sequence {
            var hx = x; var hy = this@diagonals2.size-1
            while( hx < this@diagonals2[0].length && hy >= 0) {
                yield(this@diagonals2[hy][hx])
                hy--; hx++
            }
        }.joinToString("")
        yield(diagonal)
    }
}.toList()
fun WordPuzzle.allLines() =
    this.lines() + this.rows() + this.diagonals1() + this.diagonals2() +
            this.lines().map { it. reversed() } + this.rows().map { it. reversed() } + this.diagonals1().map { it. reversed() } + this.diagonals2().map { it. reversed() }

typealias WordPuzzle = List<String>

fun List<String>.countString(regex: Regex): Int = sumOf { line ->
    regex.findAll(line).count()
}

fun WordPuzzle.findShape(shape: List<String>) = sequence {
    for (x in 0 until this@findShape[0].length)
        for (y in indices) {
            var foundShapeMismatch = false
            for (sx in 0 until shape[0].length)
                for (sy in shape.indices) {
                    val c = shape[sy][sx]
                    if (x + sx >= this@findShape[0].length || y + sy >= this@findShape.size) {
                        foundShapeMismatch = true
                    }
                    else if (c != '.')
                        if (this@findShape[y + sy][x + sx] != c) {
                            foundShapeMismatch = true
                        }

                }
            if (! foundShapeMismatch) yield(x to y)
        }
}.toList()

fun WordPuzzle.findShapes(shapes: List<List<String>>) = shapes.flatMap { shape ->
    this.findShape(shape)
}
