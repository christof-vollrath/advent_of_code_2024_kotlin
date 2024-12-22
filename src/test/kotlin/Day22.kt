import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class Day22Part1: BehaviorSpec() { init {

    Given("calculate 10 secret numbers for 123 ") {
        val numbers = calculateSecrets(123L, 10)
        Then("secrets should be calculated") {
            numbers.toList() shouldBe listOf(
                15887950L,
                16495136L,
                527345L,
                704524L,
                1553684L,
                12683156L,
                11100544L,
                12249484L,
                7753432L,
                5908254L
            )
        }
    }
    Given("4 start numbers") {
        val start = listOf(1L, 10L, 100L, 2024L)
        When("calculating secrets") {
            val secrets = start.map { it to calculateSecrets(it, 2000).last() }
            Then("should have the right result") {
                secrets.toList() shouldBe listOf (
                    1L to 8685429L,
                    10L to 4700978L,
                    100L to 15273692L,
                    2024L to 8667524L,
                )
            }
            Then("should calculate the sum") {
                secrets.sumOf { it.second } shouldBe 37327623L
            }
        }
    }

    Given("exercise input") {
        val start = readResource("inputDay22.txt")!!.split("\n").map { it.toLong() }
        Then("should be parsed") {
            start.size shouldBe 2448
        }
        When("calculate secrets") {
            val secrets = start.map { it to calculateSecrets(it, 2000).last() }
            Then("sum should be right") {
                secrets.sumOf { it.second} shouldBe 20068964552L
            }
        }
    }
}}

class Day22Part2: BehaviorSpec() { init {

    Given("example 123 ") {
        When("calculating price changes") {
            val priceChanges = calculatePriceChanges(123L, 10 - 1)
            Then("price changes should be right") {
                priceChanges.toList() shouldBe listOf(
                    0L to (-3L),
                    6L to (6L),
                    5L to (-1L),
                    4L to (-1L),
                    4L to (0L),
                    6L to (2L),
                    4L to (-2L),
                    4L to (0L),
                    2L to (-2L)
                )
            }
            Then ("associate with four changes") {
                val deltasWithPrice = associateWith4Changes(priceChanges).toList()
                deltasWithPrice[2] shouldBe (listOf(-1L, -1L, 0L, 2L) to 6L)
                val deltasMap = deltasWithPrice.toMap()
                deltasMap[listOf(-1L, -1L, 0L, 2L)] shouldBe 6L
            }
        }
    }
    Given("longer example") {
        val longerExample = listOf(1L, 2L, 3L, 2024L)
        When("calculating price changes") {
            val priceChanges = longerExample.map { calculatePriceChanges(it, 2000) }
            val deltasMaps = priceChanges.map { associateWith4Changes(it).toMapFirst() }
            Then("delta maps should be right") {
                deltasMaps[0][listOf(-2L, 1L, -1L, 3L)] shouldBe 7
                deltasMaps[1][listOf(-2L, 1L, -1L, 3L)] shouldBe 7
                deltasMaps[2][listOf(-2L, 1L, -1L, 3L)].shouldBeNull()
                deltasMaps[3][listOf(-2L, 1L, -1L, 3L)] shouldBe 9
            }
            Then("the best delta pattern should be found") {
                val bestDeltas = findBestDeltaPattern(priceChanges)
                bestDeltas.first shouldBe listOf(-2L, 1L, -1L, 3L)
                bestDeltas.second shouldBe listOf(7L, 7L, null, 9L)
                bestDeltas.second.filterNotNull().sum() shouldBe 23L
            }
        }
    }

    xGiven("exercise input") { // runs about 60 secs
        val starts = readResource("inputDay22.txt")!!.split("\n").map { it.toLong() }
        Then("should be parsed") {
            starts.size shouldBe 2448
        }
        When("calculate best delta") {
            val priceChanges = starts.map { calculatePriceChanges(it, 2000) }
            val bestDeltas = findBestDeltaPattern(priceChanges)
            Then("sum should be right") {
                bestDeltas.second.filterNotNull().sum() shouldBeGreaterThan 2244L
                bestDeltas.second.filterNotNull().sum() shouldBe 2246L
            }
        }
    }
} }

private fun calculateSecrets(start: Long, nr: Int): Sequence<Long> = sequence {
    var current = start
    repeat(nr) {
        val multiplied = current * 64L
        current = multiplied xor current
        current %= 16777216L
        val divided = current / 32
        current = divided xor current
        current %= 16777216L
        val multiplied2 = current * 2048L
        current = multiplied2 xor current
        current %= 16777216L
        yield(current)
    }
}

private fun calculatePriceChanges(start: Long, nr: Int) = sequence {
    var prev = start % 10
    for (secret in calculateSecrets(start, nr)) {
        val price =  secret % 10
        val delta =  price - prev
        prev = price
        yield(price to delta)
    }
}

private fun associateWith4Changes(priceChanges: Sequence<Pair<Long, Long>>) = sequence {
    val deltaQueue = FixedFifoQueue<Long>(4)
    for ((price, delta) in priceChanges) {
        deltaQueue.enqueue(delta)
        if (deltaQueue.size == 4) yield (deltaQueue.toList() to price)
    }
}

class FixedFifoQueue<T>(private val capacity: Int) {
    private val queue = ArrayDeque<T>()

    fun enqueue(element: T) {
        if (queue.size >= capacity) queue.removeFirst()
        queue.addLast(element)
    }

    fun toList(): List<T> = queue.toList() // Returns the current elements as a list

    val size: Int
        get() = queue.size
}

private fun findBestDeltaPattern(priceChanges: List<Sequence<Pair<Long, Long>>>): Pair<List<Long>, List<Long?>> {
    val deltasMaps = priceChanges.map { associateWith4Changes(it).toMapFirst() }
    val allDeltas = deltasMaps.flatMap { deltasMap ->
        deltasMap.keys.toSet()
    }.toSet()
    val deltasWithPriceList = allDeltas.map { delta ->
        val prices = deltasMaps.map { deltaMap -> deltaMap[delta] }
        delta to prices
    }
    return deltasWithPriceList.maxBy { it.second.filterNotNull().sum() }
}

private fun <K, V> Sequence<Pair<K, V>>.toMapFirst(): Map<K, V> {
    val result = mutableMapOf<K, V>()
    this.forEach { (k, v) ->
        result.putIfAbsent(k, v)
    }
    return result
}