import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.util.stream.Collectors

val exampleInputDay05 = """
        47|53
        97|13
        97|61
        97|47
        75|29
        61|13
        75|53
        29|13
        97|29
        53|29
        61|53
        97|53
        61|29
        47|13
        75|47
        97|75
        47|61
        75|61
        47|29
        75|13
        53|13
        
        75,47,61,53,29
        97,61,53,29,13
        75,29,13
        75,97,47,61,53
        61,13,29
        97,13,75,29,47
    """.trimIndent()

class Day05Part1: BehaviorSpec() { init {

    Given("example input") {
        val (pageOrderingRules, pagesList) = parseInputDay05(exampleInputDay05)
        Then("pager ordering rules should be parsed") {
            pageOrderingRules.size shouldBe 21
            pageOrderingRules[1] shouldBe (97 to 13)
        }
        Then(" pages list should be parsed") {
            pagesList.size shouldBe 6
            pagesList[0] shouldBe listOf(75, 47, 61, 53, 29)
        }
        When("deriving requirement after which pages a page must occure") {
            val printRequirement = deriveAfterRequirements(pageOrderingRules)
            Then("requirements should be right") {
                printRequirement.keys.size shouldBe 6
                printRequirement[47] shouldBe setOf(53, 13, 61, 29)
            }
            Then("checking a list fulfilling the requirement") {
                listOf(75, 47, 61, 53, 29).fulfillsRequirement(printRequirement) shouldBe true
            }
            Then("checking a list not fulfilling the requirement") {
                listOf(13, 29).fulfillsRequirement(printRequirement) shouldBe false
            }
            When("checking order of pages list") {
                val okPages = pagesList.filterFulfillingRequirement(printRequirement)
                Then("it should return only pages fulfilling the requirement") {
                    okPages shouldBe
                            listOf(
                                listOf(75, 47, 61, 53, 29),
                                listOf(97, 61, 53, 29, 13),
                                listOf(75, 29, 13)
                            )
                }
                Then("getting middle pages numbers") {
                    val middlePageNambers = selectMiddlePageNumbers(okPages)
                    middlePageNambers.sum() shouldBe 143
                }
            }
        }
    }

    Given("exercise input") {
        val (pageOrderingRules, pagesList) = parseInputDay05(readResource("inputDay05.txt")!!)
        val printRequirement = deriveAfterRequirements(pageOrderingRules)
        val okPages = pagesList.filterFulfillingRequirement(printRequirement)
        val sum = selectMiddlePageNumbers(okPages).sum()
        Then("should find solution") {
            sum shouldBe 4185
        }
    }
} }

class Day05Part2: BehaviorSpec() { init {

    Given("example input") {
        val (pageOrderingRules, pagesList) = parseInputDay05(exampleInputDay05)
        val printRequirement = deriveAfterRequirements(pageOrderingRules)
        Then("should find the wrong page") {
            listOf(61, 13, 29).findFirstNotMatchingRequirement(printRequirement) shouldBe (29 to 13)
        }
        Then("should correct the page order") {
            correctPageOrder(listOf(61, 13, 29), printRequirement) shouldBe listOf(61, 29, 13)
        }
        Then("should correct page order for some pages") {
            listOf(
                listOf(75, 97, 47, 61, 53),
                listOf(61, 13, 29),
                listOf(97, 13, 75, 29, 47)
            ).correctPageOrders(printRequirement) shouldBe
                listOf(
                    listOf(97, 75, 47, 61, 53),
                    listOf(61, 29, 13),
                    listOf(97, 75, 47, 29, 13)
                )
        }
        Then("should correct the page order of all pages and calculate the sum") {
            val wrongPages = pagesList.filterNotFulfillingRequirement(printRequirement)
            val correctedPages = wrongPages.correctPageOrders(printRequirement)
            val sum = selectMiddlePageNumbers(correctedPages).sum()
            sum shouldBe 123
            // also using parallel
            selectMiddlePageNumbers(wrongPages.correctPageOrdersParallel(printRequirement)).sum() shouldBe 123
        }
    }
    xGiven("exercise input") { // Takes about 25 sec even with the version using parallel streams
        val (pageOrderingRules, pagesList) = parseInputDay05(readResource("inputDay05.txt")!!)
        val printRequirement = deriveAfterRequirements(pageOrderingRules)
        val wrongPages = pagesList.filterNotFulfillingRequirement(printRequirement)
        val correctedPages = wrongPages.correctPageOrdersParallel(printRequirement)
        val sum = selectMiddlePageNumbers(correctedPages).sum()
        Then("should find solution") {
            sum shouldBeLessThan 8665
            sum shouldBe 4480
        }
    }

}}


private fun deriveAfterRequirements(pageOrderingRules: List<Pair<Int, Int>>): PrintRequirement {
    val result = HashMap<Int, MutableSet<Int>>()
    pageOrderingRules.forEach { (from, to) ->
        val existing = result[from]
        if (existing == null) result[from] = mutableSetOf(to)
        else existing.add(to)
    }
    return result
}

private fun parseInputDay05(input: String): Pair<List<Pair<Int, Int>>, List<List<Int>>> {
    val (rulesPart, pagesPart) = input.split("\n\n")
    return (parseRulesPart(rulesPart) to parsePagesPart(pagesPart))
}

private fun parseRulesPart(rulesPart: String): List<Pair<Int, Int>> = rulesPart.split("\n").map {
    it.split("|").map { it.toInt() }.toPair()
}

private fun parsePagesPart(pagesPart: String): List<List<Int>> = pagesPart.split("\n").map {
    it.split(",").map { it.toInt() }
}

typealias PrintRequirement = Map<Int, Set<Int>>

fun List<List<Int>>.filterFulfillingRequirement(requirement: PrintRequirement) = this.filter { it.fulfillsRequirement(requirement) }
fun List<List<Int>>.filterNotFulfillingRequirement(requirement: PrintRequirement) = this.filter { ! it.fulfillsRequirement(requirement) }

fun List<Int>.fulfillsRequirement(requirement: Map<Int, Set<Int>>): Boolean =
    this.findFirstNotMatchingRequirement(requirement) == null

fun List<Int>.findFirstNotMatchingRequirement(requirement: Map<Int, Set<Int>>): Pair<Int, Int>? {
    val visitedPages = mutableSetOf<Int>()
    for(page in this) {
        val mustBeAfter = requirement[page] ?: emptySet()
        val shouldBeAfter = visitedPages.intersect(mustBeAfter)
        if (shouldBeAfter.isNotEmpty()) return page to shouldBeAfter.first()
        visitedPages.add(page)
    }
    return null
}

fun List<List<Int>>.correctPageOrders(requirement: PrintRequirement) = map { correctPageOrder(it, requirement) }

fun List<List<Int>>.correctPageOrdersParallel(requirement: PrintRequirement) = parallelStream().map { correctPageOrder(it, requirement) }.collect(Collectors.toList())

fun correctPageOrder(pages: List<Int>, requirement: PrintRequirement): List<Int> {
    var corrected = pages
    while(true) {
        val nextCorrected = correctSingleError(corrected, requirement)
        if (nextCorrected == corrected) return corrected // everything corrected
        corrected = nextCorrected
    }
}

fun correctSingleError(pages: List<Int>, requirement: PrintRequirement): List<Int> {
    val result = pages.toMutableList()
    val (before, after) = pages.findFirstNotMatchingRequirement(requirement) ?: return pages
    val afterIndex = result.indexOf(after)
    result.removeAt(afterIndex)
    val beforeIndex = result.indexOf(before)
    result.add(beforeIndex + 1, after)
    return  result
}

private fun selectMiddlePageNumbers(okPages: List<List<Int>>): List<Int> = okPages.map { it[it.size / 2]}
