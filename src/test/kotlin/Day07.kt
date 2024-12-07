import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

val exampleInputDay07 = """
        190: 10 19
        3267: 81 40 27
        83: 17 5
        156: 15 6
        7290: 6 8 6 15
        161011: 16 10 13
        192: 17 8 14
        21037: 9 7 18 13
        292: 11 6 16 20
        """.trimIndent()

class Day07Part1: BehaviorSpec() { init {

    Given("an empty example with 0") {
        val result = findOperator(0, listOf())
        Then("operators should be empty") {
            result.shouldNotBeNull()
            result.first shouldBe 0
            result.second shouldBe listOf()
        }
    }
    Given("an empty example with 1") {
        val result = findOperator(1, listOf())
        Then("search should fail") {
            result.shouldBeNull()
        }
    }
    Given("an very simple example with matches") {
        val result = findOperator(5, listOf(5))
        Then("operators should be empty") {
            result.shouldNotBeNull()
            result.first shouldBe 5
            result.second shouldBe listOf()
        }
    }
    Given("an very simple example not matching") {
        val result = findOperator(1, listOf())
        Then("search should fail") {
            result.shouldBeNull()
        }
    }
    Given("a simple example where the operator is plus") {
        val result = findOperator(7, listOf(5, 2))
        Then("operator should be found") {
            result.shouldNotBeNull()
            result.first shouldBe 7
            result.second shouldBe listOf(plus)
        }
    }
    Given("a simple example where the operator is mult") {
        val result = findOperator(10, listOf(5, 2))
        Then("operator should be found") {
            result.shouldNotBeNull()
            result.first shouldBe 10
            result.second shouldBe listOf(mult)
        }
    }
    Given("a simple example where there is not matching operator") {
        val result = findOperator(99, listOf(777))
        Then("operator should be found") {
            result.shouldBeNull()
        }
    }
    Given("a longer example") {
        val result = findOperator(9, listOf(1, 2, 3))
        Then("operator should be found") {
            result.shouldNotBeNull()
            result.first shouldBe 9
            result.second shouldBe listOf(plus, mult)
        }
    }
    Given("example input") {
        val calibrationEquations = parseCalibrationEquations(exampleInputDay07)
        Then("calibration equations should be parsed") {
            calibrationEquations.size shouldBe 9
            with(calibrationEquations[0]) {
                expected shouldBe 190L
                numbers shouldBe listOf(10L, 19L)
            }
        }
        Then("finding matching operators") {
            val results = calibrationEquations.mapNotNull { findOperator(it.expected, it.numbers) }
            results.size shouldBe 3
            results.map { it.first } shouldBe listOf(190, 3267, 292)
            results.sumOf { it.first } shouldBe 3749
        }
    }

    Given("exercise input") {
        val calibrationEquations = parseCalibrationEquations(readResource("inputDay07.txt")!!)
        Then("finding matching operators") {
            val results = calibrationEquations.mapNotNull { findOperator(it.expected, it.numbers) }
            results.size shouldBe 284
            results.sumOf { it.first } shouldBe 10_741_443_549_536L
        }
    }
} }

data class CalibrationEquation(val expected: Long, val numbers: List<Long>)

private fun parseCalibrationEquations(input: String): List<CalibrationEquation> = input.split("\n").map { line ->
    val parts =line.split(""":\s+""".toRegex())
    val expected = parts[0].toLong()
    val numbers = parts[1].split("""\s+""".toRegex()).map { it.toLong()}
    CalibrationEquation(expected, numbers)
}

val plus = { a: Long, b: Long -> a + b }
val mult = { a: Long, b: Long -> a * b }

private fun findOperator(expectedResult: Long, numbers: List<Long>, ops: List<(Long, Long)->Long> = emptyList(), interimResult: Long = 0, pendingOp: ((Long, Long) -> Long)? = null): Pair<Long, List<(Long, Long)->Long>>? {
    return when {
        numbers.isEmpty() -> {
            if (interimResult == expectedResult) interimResult to ops
            else null
        }
        numbers.size == 1 -> {
            val nextOps = if (pendingOp == null) ops else ops + pendingOp
            val nextInterimResult = if (pendingOp == null) numbers.first() else pendingOp(numbers.first(), interimResult)
            findOperator(expectedResult, numbers.drop(1), nextOps, nextInterimResult)
        }
        else -> {
            val nextOps = if (pendingOp == null) ops else ops + pendingOp
            val nextInterimResult = if (pendingOp == null) numbers.first() else pendingOp(numbers.first(), interimResult)
            val nextNumbers = numbers.drop(1)
            // try out plus
            val resultPlus = findOperator(expectedResult, nextNumbers, nextOps, nextInterimResult, plus)
            if (resultPlus != null) resultPlus
            else {
                // try out mult
                findOperator(expectedResult, nextNumbers, nextOps, nextInterimResult, mult)
            }
        }
    }
}