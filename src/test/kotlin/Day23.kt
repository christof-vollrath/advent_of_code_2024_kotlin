
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

val exampleInputDay23 = """
            kh-tc
            qp-kh
            de-cg
            ka-co
            yn-aq
            qp-ub
            cg-tb
            vc-aq
            tb-ka
            wh-tc
            yn-cg
            kh-ub
            ta-co
            de-co
            tc-td
            tb-wq
            wh-td
            ta-ka
            td-qp
            aq-cg
            wq-ub
            ub-vc
            de-ta
            wq-aq
            wq-vc
            wh-yn
            ka-de
            kh-ta
            co-tc
            wh-qp
            tb-vc
            td-yn
        """.trimIndent()

class Day23Part1: BehaviorSpec() { init {

    Given("example") {
        val connections = parseConnections(exampleInputDay23)
        Then("connections should be parsed") {
            connections.size shouldBe 32
            connections[0] shouldBe ("kh" to "tc")
        }
        val allComputers = extractAllComputers(connections)
        Then("should have found all computers") {
            allComputers shouldContain "td"
            allComputers.size shouldBe 16
        }
        val computersWithConnections = findComputersWithConnections(connections)
        Then("should have found all connection for each computer") {
            computersWithConnections.size shouldBe 16
            computersWithConnections["td"]!! shouldContain("yn")
            computersWithConnections["yn"]!! shouldContain("td")
        }
        val triples = findConnectedNLets(computersWithConnections)
        Then("should find right triples") {
            triples.size shouldBe 12
            triples shouldContain setOf("co", "de", "ka")
        }
        val triplesWithT = triples.filter { computers -> computers.any { it.startsWith("t") } }
        Then("should find right triples with t") {
            triplesWithT.size shouldBe 7
            triplesWithT shouldContain setOf("co", "de", "ta")
        }
    }
    Given("exercise input") {
        val connections = parseConnections(readResource("inputDay23.txt")!!)
        Then("should be parsed") {
            connections.size shouldBe 3380
        }
        val allComputers = extractAllComputers(connections)
        allComputers.size shouldBeGreaterThan 10
        val computersWithConnections = findComputersWithConnections(connections)
        val triples = findConnectedNLets(computersWithConnections)
        val triplesWithT = triples.filter { computers -> computers.any { it.startsWith("t") } }
        Then("should find right triples with t") {
            triplesWithT.size shouldBe 1344
        }
    }
}}

class Day23Part2: BehaviorSpec() { init {
    Given("some cases for nlets") {
        val input1 = listOf("A", "B")
        Then("nlet 1") {
            createNLets(input1, 1) shouldBe listOf(listOf("A"), listOf("B"))
        }
        val input2 = listOf("A", "B")
        Then("nlet 2") {
            createNLets(input2, 2) shouldBe listOf(listOf("A", "B"))
        }
        val input3 = listOf("A", "B", "C")
        Then("nlet 2 of 3") {
            createNLets(input3, 2) shouldBe listOf(listOf("A", "B"), listOf("A", "C"), listOf("B", "C"))
        }
        val input4 = listOf("A", "B", "C", "D")
        Then("nlet 3 of 4") {
            createNLets(input4, 3) shouldBe listOf(
                listOf("A", "B", "C"), listOf("A", "B", "D"),
                listOf("A", "C", "D"),
                listOf("B", "C", "D")
            )
        }
    }
    Given("example") {
        val connections = parseConnections(exampleInputDay23)
        val computersWithConnections = findComputersWithConnections(connections)
        val nlets = findBiggestNLet(computersWithConnections)
        Then ("nlet should be found") {
            nlets.size shouldBe 1
            nlets.first().size shouldBe 4
            nlets.first() shouldBe listOf("co", "de", "ka", "ta")
        }
    }
    xGiven("exercise input") { // runs for about 40 sec
        val connections = parseConnections(readResource("inputDay23.txt")!!)
        val computersWithConnections = findComputersWithConnections(connections)
        val nlets = findBiggestNLet(computersWithConnections)
        Then ("nlet should be found") {
            nlets.size shouldBe 1
            nlets.first().size shouldBe 13
            nlets.first().joinToString(",") shouldBe "ab,al,cq,cr,da,db,dr,fw,ly,mn,od,py,uh"
        }
    }

}}

private fun parseConnections(input: String) = input.split("\n").map { line ->
    val (left, right) = line.split("-")
    left to right
}

private fun extractAllComputers(connections: List<Pair<String, String>>) =
    connections.map { it.first }.toSet() + connections.map { it.second }.toSet()

private fun findComputersWithConnections(connections: List<Pair<String, String>>): Map<String, List<String>> {
    val result = mutableMapOf<String, MutableList<String>>()
    for ((from, to) in connections) {
        val found1 = result[from]
        if (found1 == null) result.put(from, mutableListOf(to))
        else found1.add(to)
        val found2 = result[to]
        if (found2 == null) result.put(to, mutableListOf(from))
        else found2.add(from)
    }
    return result
}

private fun findConnectedNLets(computersWithConnections: Map<String, List<String>>, nr: Int = 3): Set<Set<String>> {
    val computersWithAtLeastNConnections = computersWithConnections.filter { it.value.size >= nr }
    val computersConnectedBack = computersWithAtLeastNConnections.map { listOf(it.key) + it.value.filter { connected -> computersWithAtLeastNConnections[connected]?.contains(it.key) ?: false }}
    val reduced = reduceToInterconnected(computersConnectedBack, computersWithAtLeastNConnections, nr)
    return reduced.toSet()
}

private fun reduceToInterconnected(computersList: List<List<String>>, connections: Map<String, List<String>>, nr: Int) = computersList.flatMap { computers ->
    createNLets(computers, nr).filter { checkConnections(it, connections) }.map { it.toSet() }
}

private fun createNLets(computers: List<String>, n: Int = 3): List<List<String>> =
    if (n == 0) emptyList<List<String>>()
    else if (n == 1) computers.map { listOf(it) }
    else if (n == computers.size) listOf(computers)
    else {
        val result = mutableListOf<List<String>>()
        val nexts = createNLets(computers.drop(1), n - 1)
        for (next in nexts) result.add(listOf(computers.first()) + next)
        result.addAll(createNLets(computers.drop(1), n))
        result
    }


private fun checkConnections(computers: List<String>, connections: Map<String, List<String>>): Boolean {
    var curr = computers.first()
    var remaining = computers.drop(1)
    while (remaining.isNotEmpty())  {
        for (c in remaining) {
            if (! (connections[curr]!!.contains(c))) return false
        }
        curr = remaining.first()
        remaining = remaining.drop(1)
    }
    return true
}

private fun findBiggestNLet(computersWithConnections: Map<String, List<String>>): Set<List<String>> {
    var nr = 1
    var curr: Set<Set<String>> = emptySet()
    while(true) {
        //println("checking $nr")
        val next = findConnectedNLets(computersWithConnections, nr)
        if (next.isEmpty()) return curr.map { it.sorted() }.toSet()
        curr = next
        nr++
    }
}
