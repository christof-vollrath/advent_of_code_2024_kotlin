import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class Day11Part1: BehaviorSpec() { init {

    Given("first example") {
        val example1 = parseStones("0 1 10 99 999")
        Then("should bee parsed") {
            example1 shouldBe listOf(
                BigDecimal(0),
                BigDecimal(1),
                BigDecimal(10),
                BigDecimal(99),
                BigDecimal(999)
            )
        }
        When("blinking") {
            blinkStones(example1) shouldBe listOf(
                BigDecimal(1),
                BigDecimal(2024),
                BigDecimal(1),
                BigDecimal(0),
                BigDecimal(9),
                BigDecimal(9),
                BigDecimal(2021976)
            )
        }
    }
    Given("longer example") {
        val example2 = parseStones("125 17")
        Then("blinking several times") {
            blinkStones(example2, 6) shouldBe
                    parseStones("2097446912 14168 4048 2 0 2 4 40 48 2024 40 48 80 96 2 8 6 7 6 0 3 2")
        }
        Then("blinking more") {
            blinkStones(example2, 25).size shouldBe 55312
        }
    }

    Given("exercise input") {
        val stones = parseStones("112 1110 163902 0 7656027 83039 9 74")
        Then("then blinking should create thr right result") {
            blinkStones(stones, 25).size shouldBe 183620
        }
    }
} }

fun parseStones(input: String): List<BigDecimal> = input.split(" ").mapNotNull { it.toBigDecimalOrNull() }

fun blinkStones(stones: List<BigDecimal>): List<BigDecimal> = sequence {
    stones.forEach { stone ->
        val stoneString = stone.toString()
        when {
            stone == BigDecimal(0) -> yield(BigDecimal(1))
            stoneString.length % 2 == 0 -> {
                val splitAt = stoneString.length / 2
                yield(stoneString.substring(0 until splitAt).toBigDecimalOrNull()!!)
                yield(stoneString.substring(splitAt).toBigDecimalOrNull()!!)
            }
            else -> yield(stone.multiply(BigDecimal(2024)))
        }
    }
}.toList()

private fun blinkStones(stones: List<BigDecimal>, nr: Int): List<BigDecimal>  {
    var curr = stones
    repeat(nr) {
        curr = blinkStones(curr)
    }
    return curr
}
