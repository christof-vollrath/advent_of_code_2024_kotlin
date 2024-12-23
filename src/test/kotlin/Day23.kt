import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
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

    Given("exampl") {
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
        val triples = findConnectedTriples(computersWithConnections)
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
        val computersWithConnections = findComputersWithConnections(connections)
        val triples = findConnectedTriples(computersWithConnections)
        val triplesWithT = triples.filter { computers -> computers.any { it.startsWith("t") } }
        Then("should find right triples with t") {
            triplesWithT.size shouldBe 1344
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

private fun findConnectedTriples(computersWithConnections: Map<String, List<String>>): Set<Set<String>> {
    val computersWithAtLeast3Connections = computersWithConnections.filter { it.value.size >= 3 }
    val computersConnectedBack = computersWithAtLeast3Connections.map { listOf(it.key) + it.value.filter { connected -> computersWithAtLeast3Connections[connected]?.contains(it.key) ?: false }}
    val reduced = reduceToInterconnected(computersConnectedBack, computersWithAtLeast3Connections)
    return reduced.toSet()
}

private fun reduceToInterconnected(computersList: List<List<String>>, connections: Map<String, List<String>>) = computersList.flatMap { computers ->
    createTriples(computers).filter { checkConnections(it, connections) }.map { it.toSet() }
}

private fun createTriples(computers: List<String>) = sequence {
    var remaining_1 = computers
    while (remaining_1.size >= 3) {
        val first = computers.first()
        var remaining_2 = computers.drop(1)
        while (remaining_2.size >= 2) {
            val second = remaining_2.first()
            val remaining_3 = remaining_2.drop(1)
                for (third in remaining_3) {
                    yield(listOf(first, second, third))
                }
            remaining_2 = remaining_2.drop(1)
        }
        remaining_1 = remaining_1.drop(1)
    }
}.toList()

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
