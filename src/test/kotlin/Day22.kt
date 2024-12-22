import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe


class Day22Part: BehaviorSpec() { init {

    Given("calculate 10 secret numbers for 123 ") {
        val numbers = calculateSecrets(123L, 10)
        Then("secrets should be calculated") {
            numbers shouldBe listOf(
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
                secrets shouldBe listOf (
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

fun calculateSecrets(start: Long, nr: Int): List<Long> = sequence {
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
}.toList()

