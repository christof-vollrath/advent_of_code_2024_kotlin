import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.math.absoluteValue

val exampleInputDay01 = """
        3   4
        4   3
        2   5
        1   3
        3   9
        3   3
    """.trimIndent()

class Day01Part1: BehaviorSpec() { init {

    Given("example input") {
        When("parsing note pairs") {
            val notePairs = parseNotePairs(exampleInputDay01)
            Then("should be parsed") {
                notePairs[0] shouldBe (3 to 4)
            }
            When("sorting the pairs indvidually") {
                val sortedPairs = sortPairsIndividually(notePairs)
                Then(" should be sorted") {
                    sortedPairs[0] shouldBe (1 to 3)
                }
                When("summing the diffs") {
                    val sum = pairsDistance(sortedPairs).sum()
                    Then("should find the right value") {
                        sum shouldBe 11
                    }
                }
            }
        }
    }
    Given("exercise input") {
        val notePairs = parseNotePairs(readResource("inputDay01.txt")!!)
        When("sort and sum diffs") {
            val sortedPairs = sortPairsIndividually(notePairs)
            val sum = pairsDistance(sortedPairs).sum()
            Then("it should find the solution for part 1") {
                sum shouldBe 2_176_849
            }
        }
    }
} }

private fun pairsDistance(pairs: List<Pair<Int, Int>>) = pairs.map { (it.first - it.second).absoluteValue }

private fun parseNotePairs(input: String): List<Pair<Int, Int>> =
    input.split("\n"). map {
        val stringParts = it.trim().split("""\s+""".toRegex()).map { it.toInt() }
        stringParts[0] to stringParts[1]
    }

private fun sortPairsIndividually(notePairs: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
    val list1 = notePairs.map { it.first }.sorted()
    val list2 = notePairs.map { it.second }.sorted()
    return list1.zip(list2)
}
