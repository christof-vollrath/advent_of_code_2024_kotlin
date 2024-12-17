import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.math.pow

class Day17Part1: BehaviorSpec() { init {

    Given("example 1") {
        val strangeDevice = StrangeDevice(C = 9, program = listOf(2, 6))
        Then("executing the program") {
            strangeDevice.executeProgram()
            strangeDevice.B shouldBe 1
        }
    }
    Given("example 2") {
        val strangeDevice = StrangeDevice(A = 10, program = listOf(5, 0, 5, 1, 5, 4))
        Then("executing the program") {
            strangeDevice.executeProgram()
            strangeDevice.output shouldBe listOf(0, 1, 2)
        }
    }
    Given("example 3") {
        val strangeDevice = StrangeDevice(A = 2024, program = listOf(0, 1, 5, 4, 3, 0))
        Then("executing the program") {
            strangeDevice.executeProgram()
            strangeDevice.output shouldBe listOf(4, 2, 5, 6, 7, 7, 7, 7, 3, 1, 0)
            strangeDevice.A shouldBe 0
        }
    }
    Given("example 4") {
        val strangeDevice = StrangeDevice(B = 29, program = listOf(1, 7))
        Then("executing the program") {
            strangeDevice.executeProgram()
            strangeDevice.B shouldBe 26
        }
    }
    Given("example 5") {
        val strangeDevice = StrangeDevice(B = 2024, C = 43690, program = listOf(4, 0))
        Then("executing the program") {
            strangeDevice.executeProgram()
            strangeDevice.B shouldBe 44354
        }
    }
    Given("example") {
        val strangeDevice = StrangeDevice(A = 729, program = listOf(0, 1, 5, 4, 3, 0))
        Then("executing the program") {
            strangeDevice.executeProgram()
            strangeDevice.output shouldBe listOf(4, 6, 3, 5, 6, 3, 5, 2, 1, 0)
        }
    }
    Given("exercise") {
        val strangeDevice = StrangeDevice(A = 51571418, program = listOf(2, 4, 1, 1, 7, 5, 0, 3, 1, 4, 4, 5, 5, 5, 3, 0))
        Then("executing the program") {
            strangeDevice.executeProgram()
            val solution = strangeDevice.output.joinToString(",")
            solution shouldNotBe "404712716"
            solution shouldBe "4,0,4,7,1,2,7,1,6"
        }
    }
}}

class Day17Part2: BehaviorSpec() { init {
    Given("example") {
        val solution = findAForQuine(listOf(0, 3, 5, 4, 3, 0))
        Then("shouldFindTheResult") {
            solution shouldBe 117440
        }
    }
    xGiven("exercise") { // doesn't find a solution when search even up to Int.MAX_VAL
        val solution = findAForQuine(listOf(2, 4, 1, 1, 7, 5, 0, 3, 1, 4, 4, 5, 5, 5, 3, 0))
        Then("shouldFindTheResult") {
            solution shouldBe 0
        }
    }
}}

private class StrangeDevice(
    var A: Int = 0,
    var B: Int = 0,
    var C: Int = 0,
    var IP: Int = 0,
    val program: List<Int>,
    val output: MutableList<Int> = mutableListOf()
) {
    fun executeProgram() {
        while(IP < program.size - 1) {
            val opcode = program[IP]
            val operand = program[IP + 1]
            when(opcode) {
                0 -> adv(evaluateCombo(operand))
                1 -> bxl(operand)
                2 -> bst(evaluateCombo(operand))
                3 -> if (A != 0) IP = operand - 2 // compensation because later + 2 happens
                4 -> bxc()
                5 -> out(evaluateCombo(operand))
                6 -> bdv(evaluateCombo(operand))
                7 -> cdv(evaluateCombo(operand))
                else -> throw IllegalArgumentException("Illegal opcode $opcode")
            }
            IP += 2
        }
    }
    fun executeProgramForOutput(expectedOutput: List<Int>) {
        while(IP < program.size - 1) {
            if (output.size > 0 && expectedOutput.take(output.size) != output) return // it output already differs break execution
            val opcode = program[IP]
            val operand = program[IP + 1]
            when(opcode) {
                0 -> adv(evaluateCombo(operand))
                1 -> bxl(operand)
                2 -> bst(evaluateCombo(operand))
                3 -> if (A != 0) IP = operand - 2 // compensation because later + 2 happens
                4 -> bxc()
                5 -> out(evaluateCombo(operand))
                6 -> bdv(evaluateCombo(operand))
                7 -> cdv(evaluateCombo(operand))
                else -> throw IllegalArgumentException("Illegal opcode $opcode")
            }
            IP += 2
        }
    }
    fun evaluateCombo(combo: Int): Int =
        when(combo) {
            in 0 .. 3 -> combo
            4 -> A
            5 -> B
            6 -> C
            else -> throw IllegalArgumentException("Illegal combo $combo")
        }


    fun adv(op: Int) {
        val denominator = 2.toDouble().pow(op.toDouble())
        A = (A / denominator).toInt()
    }
    fun bxl(op: Int) {
        B = B xor op
    }
    fun bst(op: Int) {
        B = op % 8
    }
    fun bxc() {
        B = B xor C
    }
    fun out(op: Int) {
        output.add(op % 8)
    }
    fun bdv(op: Int) {
        val denominator = 2.toDouble().pow(op)
        B = (A / denominator).toInt()
    }
    fun cdv(op: Int) {
        val denominator = 2.toDouble().pow(op)
        C = (A / denominator).toInt()
    }
}

private fun findAForQuine(program: List<Int>): Int {
    val max = Int.MAX_VALUE
    for (i in 0..max) {
        if (i % 1000_000 == 0) println("checking $i")
        val strangeDevice = StrangeDevice(A = i, program = program)
        strangeDevice.executeProgramForOutput(program)
        if (strangeDevice.output == program) return i
    }
    throw IllegalStateException("No quine found up to $max")
}
