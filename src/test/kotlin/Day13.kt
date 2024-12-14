import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInputDay13 = """
            Button A: X+94, Y+34
            Button B: X+22, Y+67
            Prize: X=8400, Y=5400
        
            Button A: X+26, Y+66
            Button B: X+67, Y+21
            Prize: X=12748, Y=12176
        
            Button A: X+17, Y+86
            Button B: X+84, Y+37
            Prize: X=7870, Y=6450
        
            Button A: X+69, Y+23
            Button B: X+27, Y+71
            Prize: X=18641, Y=10279
        """.trimIndent()

class Day13Part1: BehaviorSpec() { init {

    Given("example input") {
        val clawMachines = parseClawMachines(exampleInputDay13)
        Then("should be parsed") {
            clawMachines.size shouldBe 4
            clawMachines[0].buttons shouldBe listOf(ClawMachineButton(94, 34), ClawMachineButton(22, 67))
            clawMachines[0].prize shouldBe ClawMachinePrize(8400, 5400)
        }
        Then("should find the chipest way to push buttons on first machine") {
            findPushButtons(clawMachines[0]) shouldBe (280 to listOf(80, 40))
        }
        Then("should find the chipest way to push buttons for all machines") {
            clawMachines.map { findPushButtons(it) } shouldBe listOf(
                280 to listOf(80, 40),
                null,
                200 to listOf(38, 86),
                null
            )
        }
        Then("should find sum of pushes to win all possible") {
            clawMachines.mapNotNull { findPushButtons(it) }.sumOf { it.first } shouldBe 480
        }
    }
    Given("exercise input") {
        val clawMachines = parseClawMachines(readResource("inputDay13.txt")!!)
        When("finding buttons pushs for all machines") {
            val solutions = clawMachines.mapNotNull { findPushButtons(it) }
            val sum = solutions.sumOf { it.first }
            Then("it should find the solution for part 1") {
                sum shouldBe 26005
            }
        }
    }
} }

typealias ClawMachineButton = Coord2
typealias ClawMachinePrize = Coord2
data class ClawMachine(val buttons: List<ClawMachineButton>, val prize: ClawMachinePrize)

private fun parseClawMachines(input: String): List<ClawMachine> {
    fun parseButtonLine(line: String): ClawMachineButton {
        val regex = """Button .: X\+(\d+), Y\+(\d+)""".toRegex()
        val nums = regex
            .matchEntire(line)
            ?.destructured
            ?.toList()
            ?.map { it.toInt() }
            ?: throw IllegalArgumentException("could not parse buttons in $line")
        return ClawMachineButton(nums[0], nums[1])
    }
    fun parsePriceLine(line: String): ClawMachinePrize {
        val regex = """Prize: X=(\d+), Y=(\d+)""".toRegex()
        val nums = regex
            .matchEntire(line)
            ?.destructured
            ?.toList()
            ?.map { it.toInt() }
            ?: throw IllegalArgumentException("could not parse buttons in $line")
        return ClawMachinePrize(nums[0], nums[1])
    }
    return input.split("\n\n").map { machineString ->
        val buttons = mutableListOf<ClawMachineButton>()
        var price: ClawMachinePrize? = null
        val machineLines = machineString.split("\n")
        machineLines.forEach { line ->
            when {
                line.startsWith("Button") -> buttons.add(parseButtonLine(line))
                line.startsWith("Prize") -> price = parsePriceLine(line)
                else -> throw IllegalArgumentException("Unkown line $line")
            }
        }
        ClawMachine(buttons, price!!)
    }
}

private const val MAX_PUSHS = 100

private fun findPushButtons(machine: ClawMachine): Pair<Int, List<Int>>? {
    fun calcCosts(a: Int, b: Int) = 3 * a + b

    var currCosts = Int.MAX_VALUE
    var currSolution: List<Int>? = null

    A@ for (a in 0..MAX_PUSHS)
        B@ for (b in 0..MAX_PUSHS) {
            val coord = Coord2(a * machine.buttons[0].x, a * machine.buttons[0].y) +
                    Coord2(b * machine.buttons[1].x, b * machine.buttons[1].y)
            if (coord == machine.prize) {
                val costs = calcCosts(a, b)
                if (costs < currCosts) {
                    currCosts = costs
                    currSolution = listOf(a, b)
                }
            } else { // check for early breaks
                if (a * machine.buttons[0].x > machine.prize.x) continue@A
                if (a * machine.buttons[0].y > machine.prize.y) continue@A
                if (b * machine.buttons[0].x > machine.prize.x) continue@B
                if (b * machine.buttons[0].y > machine.prize.y) continue@B
            }
        }
    return if (currSolution != null) currCosts to currSolution
    else null
}