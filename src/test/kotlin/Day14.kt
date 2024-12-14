import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInputDay14 = """
            p=0,4 v=3,-3
            p=6,3 v=-1,-3
            p=10,3 v=-1,2
            p=2,0 v=2,-1
            p=0,0 v=1,3
            p=3,0 v=-2,-2
            p=7,6 v=-1,-3
            p=3,0 v=-1,-2
            p=9,3 v=2,3
            p=7,3 v=-1,2
            p=2,4 v=2,-3
            p=9,5 v=-3,-3
        """.trimIndent()

class Day14Part1: BehaviorSpec() { init {

    Given("example input") {
        val securityRobots = parseSecurityRobots(exampleInputDay14)
        Then("should be parsed") {
            securityRobots.size shouldBe 12
            securityRobots[11] shouldBe SecurityRobot(Coord2(9, 5), Coord2(-3, -3))
        }
        Given("bathroom") {
            val bathroom = Coord2(11, 7)
            Given("one robot near the wall") {
                val securityRobot = SecurityRobot(Coord2(9, 0), Coord2(1, 0))
                When ("moving the robot one second") {
                    moveRobot(securityRobot, bathroom)
                    Then("robot should be close to the wall") {
                        securityRobot.position shouldBe Coord2(10, 0)
                    }
                }
                When ("moving the robot one more second") {
                    moveRobot(securityRobot, bathroom)
                    Then("robot should teleported") {
                        securityRobot.position shouldBe Coord2(0, 0)
                    }
                }
            }
            Then("printing the robots") {
                printSecurityRobots(securityRobots, bathroom) shouldBe """
                    1.12.......
                    ...........
                    ...........
                    ......11.11
                    1.1........
                    .........1.
                    .......1...
                """.trimIndent()
            }
            When("moving robots 100 times") {
                moveRobots(securityRobots, bathroom, 100)
                Then("should be at right positons") {
                    printSecurityRobots(securityRobots, bathroom) shouldBe """
                        ......2..1.
                        ...........
                        1..........
                        .11........
                        .....1.....
                        ...12......
                        .1....1....
                    """.trimIndent()
                }
            }
            When("counting robots in quadrant") {
                val counts = countRobotsInQuadrat(securityRobots, bathroom)
                counts shouldBe listOf(1, 3, 4, 1)
                counts.fold(1) { a, c -> a * c } shouldBe 12
            }
        }
    }
    Given("exercise input") {
        val securityRobots = parseSecurityRobots(readResource("inputDay14.txt")!!)
        val bathroom = Coord2(101, 103)
        When("robots have moved") {
            moveRobots(securityRobots, bathroom, 100)
            val counts = countRobotsInQuadrat(securityRobots, bathroom)
            val solution = counts.fold(1) { a, c -> a * c }
            Then("should have right safety factor") {
                solution shouldBe 225_810_288
            }
        }
    }
} }

data class SecurityRobot(var position: Coord2, val velocity: Coord2)

private fun parseSecurityRobots(input: String): List<SecurityRobot> = input.split("\n").map { line ->
    val regex = """p=(-?\d+),(-?\d+) v=(-?\d+),(-?\d+)""".toRegex()
    val nums = regex
        .matchEntire(line)
        ?.destructured
        ?.toList()
        ?.map { it.toInt() }
        ?: throw IllegalArgumentException("could not parse buttons in $line")
    SecurityRobot(Coord2(nums[0], nums[1]), Coord2(nums[2], nums[3]))
}

private fun moveRobots(securityRobots: List<SecurityRobot>, room: Coord2, nr: Int) =
    repeat(nr) { moveRobots(securityRobots, room) }

private fun moveRobots(securityRobots: List<SecurityRobot>, room: Coord2) {
    securityRobots.forEach {
        moveRobot(it, room)
    }
}

private fun moveRobot(securityRobot: SecurityRobot, room: Coord2) {
    fun teleport(a: Int, roomSize: Int) =
        if (a >= roomSize) a - roomSize
        else if (a < 0) roomSize + a
        else a
    fun teleport(a: Coord2, room: Coord2): Coord2 =
        Coord2(teleport(a.x, room.x), teleport(a.y, room.y))

    securityRobot.position = teleport(securityRobot.position + securityRobot.velocity, room)
}

private fun printSecurityRobots(securityRobots: List<SecurityRobot>, room: Coord2): String {
    println(securityRobots.size)
    val robotMap = securityRobots.groupBy { it.position }
    return (0 until room.y).map { y ->
        (0 until room.x).map { x ->
            val robots = robotMap[Coord2(x, y)]
            if (robots == null) "."
            else robots.size
        }.joinToString("")
    }.joinToString("\n")

}

private fun countRobotsInQuadrat(robots: List<SecurityRobot>, room: Coord2): List<Int> {
    val quadrants = listOf(
        Coord2(0, 0) to Coord2(room.x / 2 - 1, room.y / 2 - 1),
        Coord2(room.x / 2 + 1, 0) to Coord2(room.x - 1, room.y / 2 - 1),
        Coord2(0, room.y / 2 + 1) to Coord2(room.x / 2 - 1,  room.y - 1),
        Coord2(room.x / 2 + 1, room.y / 2 + 1) to Coord2(room.x - 1, room.y - 1)
    )
    return quadrants.map { (upperLeft, lowerRight) ->
        robots.filter { robot ->
            upperLeft.x <= robot.position.x && robot.position.x <= lowerRight.x &&
            upperLeft.y <= robot.position.y && robot.position.y <= lowerRight.y
        }.count()
    }
}

