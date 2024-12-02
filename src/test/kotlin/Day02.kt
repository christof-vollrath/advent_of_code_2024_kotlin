import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
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
            Then("checking safety of all reports") {
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
        Then("Checking save example") {
            safetyCheck(listOf(7, 6, 4, 2, 1)) shouldBe true
        }
        Then("Checking unsave example") {
            safetyCheck(listOf(1, 3, 2, 4, 5)) shouldBe false
        }
    }
    Given("exercise input") {
        val reactorReports = parseReactorReports(readResource("inputDay02.txt")!!)
        When("checking safety of all reports") {
            val checkResult = reactorReports.map { safetyCheck(it) }
            Then("result should be found") {
                checkResult.count { it } shouldBe  549
            }
        }
    }
} }


class Day02Part2: BehaviorSpec() { init {

    Given("example input") {
        val reactorReports = parseReactorReports(exampleInputDay02)
        When("find bad level") {
            When("checking bad levels of all reports") {
                val checkResult = reactorReports.map { findBadLevel(it) }
                Then("All bad levels should be found") {
                    checkResult shouldBe listOf(null, 1, 2, 1, 2, null)
                }
            }
        }
        Then("checking tolerantly for bad levels") {
            val checkResult = reactorReports.map { safetyCheckTolerant(it) }
            checkResult shouldBe listOf(true, false, false, true, true, true)
            checkResult.count { it } shouldBe 4
        }
    }
    Given("examples") {
        Then("it should check them tolerantly") {
            safetyCheckTolerant(listOf(14, 13, 15, 17, 19, 21)) shouldBe true // Just remove 14
            safetyCheckTolerant(listOf(13, 15, 17, 19, 21, 21)) shouldBe true // Just remove 21
            safetyCheckTolerant(listOf(14, 10, 17, 19, 21)) shouldBe true // Just remove 10
            safetyCheckTolerant(listOf(3, 1, 2, 5, 6)) shouldBe true // Just remove 3
        }
    }
    Given("exercise input") {
        val reactorReports = parseReactorReports(readResource("inputDay02.txt")!!)
        When("checking tolerantly safety of all reports") {
            val checkResult = reactorReports.map { safetyCheckTolerant(it) }
            Then("result should be found") {
                checkResult.count { it } shouldBeGreaterThan  549
                checkResult.count { it } shouldBe 589
            }
        }
    }
} }

private fun parseReactorReports(input: String): List<List<Int>> =
    input.split("\n"). map { reportLine ->
        reportLine.trim().split("""\s+""".toRegex()).map { it.toInt() }
    }


private fun safetyCheck(list: List<Int>): Boolean = findBadLevel(list) == null

private fun findBadLevel(list: List<Int>): Int? {
    val safetyChecker = createSafetyChecker(list) ?: return 0
    val zippedElements = list.zip(list.drop(1))
    zippedElements.forEachIndexed { i, p ->
        val x = p.first
        val y = p.second
        if ( !safetyChecker(x, y)) return i
    }
    return null // all checked
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

private fun safetyCheckTolerant(list: List<Int>): Boolean {
    val badLevel = findBadLevel(list) ?: return true
    // first try, remove first bad element
    val modifiedList1 = list.toMutableList()
    modifiedList1.removeAt(badLevel)
    if (safetyCheck(modifiedList1)) return true
    // second try, removes element after the bad one
    val modifiedList2 = list.toMutableList()
    modifiedList2.removeAt(badLevel + 1)
    if (safetyCheck(modifiedList2)) return true
    // third try, remove very first element because it could cause a wong decision about in/decreasing
    val modifiedList3 = list.toMutableList()
    modifiedList3.removeAt(0)
    return safetyCheck(modifiedList3)
}


