import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe


class Day03Part1: BehaviorSpec() { init {
    val exampleInputDay03Part1 = "xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))"

    Given("example input") {
        When("splitting to lines starting with mul") {
            val mulLines = splitMulLines(exampleInputDay03Part1)
            Then("should have 7 lines") {
                mulLines.size shouldBe 7
            }
            Then("parsing mul arguments") {
                parseMulArguments(mulLines[0]) shouldBe null // no arguments in "x"
                parseMulArguments(mulLines[1]) shouldBe ( 2L to 4L) // arguments in (2,4)%&
            }
        }
        When("executing all mul instructions") {
            val result = executeMul(exampleInputDay03Part1)
            Then("result should execute multiplications") {
                result shouldBe listOf(8L, 25L, 88L, 40L)
            }
            Then("sum should be right") {
                result.sum() shouldBe 161L
            }
        }
    }

    Given("exercise input") {
        val inputLines = readResource("inputDay03.txt")!!
        val result = executeMul(inputLines).sum()

        Then("should parse right and calculate sum") {
            result shouldBeGreaterThan 195_814_350L
            result shouldBe 196_826_776L
        }
    }
} }


class Day03Part2: BehaviorSpec() { init {
    val exampleInputDay03Part2 = "xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))"

    Given("example input") {
        When("splitting to lines starting with mul, do or don't") {
            val mulLines = splitMulLinesExtended(exampleInputDay03Part2)
            Then("should have 9 lines") {
                mulLines.size shouldBe 9
            }
        }
        When("executing all mul instructions") {
            val result = executeMulExtended(exampleInputDay03Part2)
            Then("result should execute multiplications") {
                result shouldBe listOf(8L, 40L)
            }
            Then("sum should be right") {
                result.sum() shouldBe 48L
            }
        }
    }
    Given("exercise input") {
        val inputLines = readResource("inputDay03.txt")!!
        val result = executeMulExtended(inputLines).sum()


        Then("should parse right and calculate sum") {
            result shouldBeLessThan  114_961_848L
            result shouldBe 106_780_429L
        }
    }

} }


private fun executeMul(input: String): List<Long> =
    splitMulLines(input).mapNotNull { line ->
        val args = parseMulArguments(line)
        if (args != null) args.first * args.second
        else null
    }

private fun parseMulArguments(input: String): Pair<Long, Long>? {
    val regex = """mul\((\d+),(\d+)\).*""".toRegex()
    val nums = regex
        .matchEntire(input)
        ?.destructured
        ?.toList()
        ?.map { it.toLong() }
        ?: return null
    return nums[0] to nums[1]
}

private fun executeMulExtended(input: String): List<Long> {
    var mulEnabled = true
    return splitMulLinesExtended(input).mapNotNull { line ->
        if (line.startsWith("don't()")) {
            mulEnabled = false
            null
        }
        else if (line.startsWith("do()")) {
            mulEnabled = true
            null
        }
        else {
            val args = parseMulArguments(line)
            if (args != null && mulEnabled) args.first * args.second
            else null
        }
    }
}

fun splitMulLines(input: String) = input.replace("mul", "\nmul").split("\n") // split input more for faster parsing and keep 'mul'
fun splitMulLinesExtended(input: String) = input
    .replace("mul", "\nmul")
    .replace("do()", "\ndo()")
    .replace("don't()", "\ndon't()")
    .split("\n")

