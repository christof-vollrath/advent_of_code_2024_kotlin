import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.math.absoluteValue

val exampleInputDay02 = """
        7 6 4 2 1
        1 2 7 8 9
        9 7 6 2 1
        1 3 2 4 5
        8 6 4 4 1
        1 3 6 7 9
    """.trimIndent()

class Day02Part1: BehaviorSpec() { init {

    Given("example input") {
        When("parsing reactor reports") {
            val reactorReports = parseReactorReports(exampleInputDay02)
            Then("should be parsed") {
                reactorReports[0] shouldBe listOf(7, 6, 4, 2, 1)
            }
            When("checking safety of all reports") {
                val checkResult = reactorReports.map { safetyCheck(it) }
                checkResult shouldBe listOf(true, false, false, false, false, true)
                checkResult.count { it } shouldBe 2
            }
        }
        When("finding safety checker") {
            val safetyChecker = createSafetyChecker(listOf(7, 6, 4, 2, 1))
            safetyChecker.shouldNotBeNull()
            Then("should be the right safety checker") {
                safetyChecker(2, 2) shouldBe false // diff should not be 0
                safetyChecker(7, 3) shouldBe false // diff should not be bigger than 3
                safetyChecker(4, 3) shouldBe true
                safetyChecker(3, 4) shouldBe false // should be descending
            }
        }
        When("Checking save example") {
            safetyCheck(listOf(7, 6, 4, 2, 1)) shouldBe true
        }
        When("Checking unsave example") {
            safetyCheck(listOf(1, 3, 2, 4, 5)) shouldBe false
        }
    }
    Given("exercise input") {
        val reactorReports = parseReactorReports(readResource("inputDay02.txt")!!)
        When("checking safety of all reports") {
            val checkResult = reactorReports.map { safetyCheck(it) }
            checkResult.count { it } shouldBe 549
        }
    }
}

    private fun safetyCheck(list: List<Int>): Boolean {
        val safetyChecker = createSafetyChecker(list) ?: return false
        val zippedElements = list.zip(list.drop(1))
        return zippedElements.all {
            val x = it.first
            val y = it.second
            safetyChecker(x, y)
        }
    }

    private fun createSafetyChecker(list: List<Int>): ((Int, Int) -> Boolean)? {
        fun basicCheck(diff: Int) = diff != 0 && diff.absoluteValue <= 3
        val diffFirst = list[0] - list[1]
        return if (!basicCheck(diffFirst)) null
        else if (diffFirst > 0)
            { x: Int, y: Int ->
                val diff = x - y
                basicCheck(diff) && diff > 0
            }
        else
            { x: Int, y: Int ->
                val diff = x - y
                basicCheck(diff) && diff < 0
            }
    }
}


class Day02Part2: BehaviorSpec() { init {

    Given("example input") {
        val reactorReports = parseReactorReports(exampleInputDay02)
    }
} }

private fun parseReactorReports(input: String): List<List<Int>> =
    input.split("\n"). map { reportLine ->
        reportLine.trim().split("""\s+""".toRegex()).map { it.toInt() }
    }
