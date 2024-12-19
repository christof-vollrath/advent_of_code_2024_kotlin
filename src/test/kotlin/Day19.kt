import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import java.lang.IllegalStateException

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

class Day19Part2: BehaviorSpec() { init {

    Given("example") {
        val (towels, designs) = parseInputDay19(exampleInputDay19)
        When("single example") {
            val design = "gbbr"
            Then("searching designs") {
                val possibleDesign = findPossibleDesign2(design, towels)
                possibleDesign.size shouldBe 4
            }
            Then("only counting") {
                val count = countPossibleDesign(design, towels)
                count shouldBe 4
            }
        }
        When("another example") {
            val design = "rrbgbr"
            Then("searching designs") {
                val possibleDesign = findPossibleDesign2(design, towels)
                possibleDesign.size shouldBe 6
            }
            Then("only counting") {
                val count = countPossibleDesign(design, towels)
                count shouldBe 6
            }
        }
        When("searching for all possible designs") {
            val possibleDesign = findPossibleDesigns2(designs, towels)
            val result = possibleDesign.map { (_, solutions) -> solutions.count() }.sum()
            Then("should find 16") {
                result shouldBe 16
            }
        }
        When("just counting possible designs") {
            val counts = countPossibleDesigns(designs, towels)
            Then("should also find 16") {
                counts shouldBe listOf(2, 1, 4, 6, 0, 1, 2, 0)
            }
        }
    }

    Given("exercise input") {
        val (towels, designs) = parseInputDay19(readResource("inputDay19.txt")!!)
        When("counting all possible designs") {
            val counts = countPossibleDesigns(designs, towels)
            Then("should have the right sum") {
                val sum = counts.sum()
                sum shouldBeGreaterThan 7951858249L
                sum shouldBe 692575723305545L
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

private fun findPossibleDesigns2(designs: List<String>, towels: List<String>): List<Pair<String, List<List<String>>>> =
    designs.map { design ->
        //println("Searching towels for $design")
        val towelsForDesign = findPossibleDesign2(design, towels)
        //println("Found ${towelsForDesign.size} for $design")
        design to towelsForDesign
    }

private fun findPossibleDesign2(design: String, towels: List<String>): List<List<String>> {
    val solution = mutableListOf<List<String>>()
    var currSearching = mutableListOf<Pair<String, List<String>>>()
    towels.forEach { towel ->
        if (design.startsWith(towel)) currSearching.add(towel to mutableListOf(towel))
    }
    while(true) {
        val nextSearching = mutableListOf<Pair<String, List<String>>>()
        for (curr in currSearching) {
            for (nextTowel in towels) {
                val nextDesign = curr.first + nextTowel
                val nextTowels = curr.second + nextTowel
                if (nextDesign == design) { // found!
                    solution.add(nextTowels)
                }
                if (nextDesign.length < design.length && design.startsWith(nextDesign)) {
                    nextSearching.add(nextDesign to nextTowels)
                }
            }
        }
        if (nextSearching.isEmpty()) return solution // nothing more to find
        //println("searching next ${nextSearching.map {it.first}}")
        currSearching = nextSearching
    }
}

private fun countPossibleDesigns(designs: List<String>, towels: List<String>): List<Long> =
    designs.map { design ->
        //println("Searching towels for $design")
        val count = countPossibleDesign(design, towels)
        //println("Found $count for $design")
        count
    }

private fun countPossibleDesign(design: String, towels: List<String>): Long {
    val counts = mutableMapOf<Int, Pair<Long, String>>() // contains counts up to pos
    towels.forEach { towel ->
        if (design.startsWith(towel)) {
            counts[towel.length] = 1L to design.substring(towel.length)
        }
    }
    for (i in 1 until design.length) {
        if(counts.contains(i)) {
            val (count, remaining) = counts[i]!!
            towels.forEach { towel ->
                if (remaining.startsWith(towel)) {
                    val next = i + towel.length
                    val existing = counts[next]
                    if (existing != null) {
                        if (remaining.substring(towel.length) != existing.second) throw IllegalStateException("expected $remaining at $i but found ${existing.second}")
                        counts[next] = existing.first + count to existing.second
                    } else counts[next] = count to remaining.substring(towel.length)
                }
            }
        }
    }
    return counts[design.length]?.first ?: 0L
}