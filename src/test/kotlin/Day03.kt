import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

val exampleInputDay03 = "xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))"

class Day03Part1: BehaviorSpec() { init {

    Given("example input") {
        When("splitting to lines starting with mul") {
            val mulLines = splitMulLines(exampleInputDay03)
            Then("should have 6 lines") {
                mulLines.size shouldBe 7
            }
            Then("parsing mul arguments") {
                parseMulArguments(mulLines[0]) shouldBe null // no arguments in "x"
                parseMulArguments(mulLines[1]) shouldBe ( 2L to 4L) // arguments in (2,4)%&
            }
        }
        When("executing all mul instructions") {
            val result = executeMul(exampleInputDay03)
            Then("result should execute multiplications") {
                result shouldBe listOf(8, 25, 88, 40)
            }
            Then("sum should be right") {
                result.sum() shouldBe 161
            }
        }
    }
    Given("exercise input") {
        val inputLines = readResource("inputDay03.txt")!!.split("\n") // Input is split in 6 lines
        val result = inputLines.sumOf { line ->
            executeMul(line).sum() // sum of each line
        }

        Then("should parse right and calculate sum") {
            result shouldBeGreaterThan 195_814_350L
            result shouldBe 196_826_776L
        }
    }
}

    private fun executeMul(input: String): List<Long> =
        splitMulLines(input).mapNotNull { line ->
            val args = parseMulArguments(line)
            if (args != null) args.first * args.second
            else null
        }

    private fun parseMulArguments(input: String): Pair<Long, Long>? {
        val regex = """\((\d+),(\d+)\).*""".toRegex()
        val nums = regex
            .matchEntire(input)
            ?.destructured
            ?.toList()
            ?.map { it.toLong() }
            ?: return null
        return nums[0] to nums[1]
    }
}


class Day03Part2: BehaviorSpec() { init {
} }

fun splitMulLines(input: String) = input.split("mul") // split input more for faster parsing

