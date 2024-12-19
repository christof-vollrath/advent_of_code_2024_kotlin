import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInputDay19 = """
            r, wr, b, g, bwu, rb, gb, br
            
            brwrr
            bggr
            gbbr
            rrbgbr
            ubwu
            bwurrg
            brgr
            bbrgwb
        """.trimIndent()


class Day19Part1: BehaviorSpec() { init {

    Given("example") {
        val (towels, designs) = parseInputDay19(exampleInputDay19)
        Then("should be parsed") {
            towels.size shouldBe 8
            towels[0] shouldBe "r"
            designs.size shouldBe 8
            designs[0] shouldBe "brwrr"
        }
        When("searching for possible designs") {
            val possibleDesign = findPossibleDesigns(designs, towels)
            Then("should find 6") {
                possibleDesign.size shouldBe 6
            }
        }
    }

    Given("exercise input") {
        val (towels, designs) = parseInputDay19(readResource("inputDay19.txt")!!)
        Then("should be parsed") {
            towels.size shouldBe 447
            designs.size shouldBe 400
        }
        When("searching for possible designs") {
            val possibleDesign = findPossibleDesigns(designs, towels)
            Then("should find 6") {
                possibleDesign.size shouldBe 319
            }
        }
    }
}}

private fun parseInputDay19(input: String): Pair<List<String>, List<String>> {
    val (part1, part2) = input.split("\n\n")
    return part1.split(""",\s*""".toRegex()) to part2.split("\n")
}

private fun findPossibleDesigns(designs: List<String>, towels: List<String>): List<Pair<String, List<String>>> =
    designs.mapNotNull { design ->
        // println("Searching towels for $design")
        val towelsForDesign = findPossibleDesign(design, towels)
        // println("Needing ${towels?.size ?: "null"} towels")
        if (towelsForDesign == null) null
        else design to towelsForDesign
    }

private fun findPossibleDesign(design: String, towels: List<String>): List<String>? {
    var currSearching = mutableMapOf<String, List<String>>()
    towels.forEach { towel ->
        if (towel == design) return listOf(towel)
        if (design.startsWith(towel)) currSearching.put(towel, mutableListOf(towel))
    }
    while(true) {
        val nextSearching = mutableMapOf<String, List<String>>()
        for (curr in currSearching) {
            for (nextTowel in towels) {
                val nextDesign = curr.key + nextTowel
                val nextTowels = curr.value + nextTowel
                if (nextDesign == design) return nextTowels // found!
                if (nextDesign.length < design.length && design.startsWith(nextDesign) && !currSearching.contains(nextDesign)) {
                    // - should not be too long
                    // - must match the begining
                    // - if already found a combination ignore this one. The already found one is shorter anyway.
                    nextSearching.put(nextDesign, nextTowels)
                }
            }
        }
        if (nextSearching.isEmpty()) return null // nothin found!
        // println("searching next ${nextSearching.map {it.key}}")
        currSearching = nextSearching
    }
}