import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInputDay15 = """
            ##########
            #..O..O.O#
            #......O.#
            #.OO..O.O#
            #..O@..O.#
            #O#..O...#
            #O..O..O.#
            #.OO.O.OO#
            #....O...#
            ##########
            
            <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
            vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
            ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
            <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
            ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
            ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
            >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
            <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
            ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
            v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
        """.trimIndent()

val testInputDay15 = """
            Initial state:
            ########
            #..O.O.#
            ##@.O..#
            #...O..#
            #.#.O..#
            #...O..#
            #......#
            ########
            
            Move <:
            ########
            #..O.O.#
            ##@.O..#
            #...O..#
            #.#.O..#
            #...O..#
            #......#
            ########
            
            Move ^:
            ########
            #.@O.O.#
            ##..O..#
            #...O..#
            #.#.O..#
            #...O..#
            #......#
            ########
            
            Move ^:
            ########
            #.@O.O.#
            ##..O..#
            #...O..#
            #.#.O..#
            #...O..#
            #......#
            ########
            
            Move >:
            ########
            #..@OO.#
            ##..O..#
            #...O..#
            #.#.O..#
            #...O..#
            #......#
            ########
            
            Move >:
            ########
            #...@OO#
            ##..O..#
            #...O..#
            #.#.O..#
            #...O..#
            #......#
            ########
            
            Move >:
            ########
            #...@OO#
            ##..O..#
            #...O..#
            #.#.O..#
            #...O..#
            #......#
            ########
            
            Move v:
            ########
            #....OO#
            ##..@..#
            #...O..#
            #.#.O..#
            #...O..#
            #...O..#
            ########
            
            Move v:
            ########
            #....OO#
            ##..@..#
            #...O..#
            #.#.O..#
            #...O..#
            #...O..#
            ########
            
            Move <:
            ########
            #....OO#
            ##.@...#
            #...O..#
            #.#.O..#
            #...O..#
            #...O..#
            ########
            
            Move v:
            ########
            #....OO#
            ##.....#
            #..@O..#
            #.#.O..#
            #...O..#
            #...O..#
            ########
            
            Move >:
            ########
            #....OO#
            ##.....#
            #...@O.#
            #.#.O..#
            #...O..#
            #...O..#
            ########
            
            Move >:
            ########
            #....OO#
            ##.....#
            #....@O#
            #.#.O..#
            #...O..#
            #...O..#
            ########
            
            Move v:
            ########
            #....OO#
            ##.....#
            #.....O#
            #.#.O@.#
            #...O..#
            #...O..#
            ########
            
            Move <:
            ########
            #....OO#
            ##.....#
            #.....O#
            #.#O@..#
            #...O..#
            #...O..#
            ########
            
            Move <:
            ########
            #....OO#
            ##.....#
            #.....O#
            #.#O@..#
            #...O..#
            #...O..#
            ########
        """.trimIndent()

class Day15Part1: BehaviorSpec() { init {

    Given("test input") {
        val (startRobot, startWarehouse, tests) = parseTestInput(testInputDay15)
        Then("should be parsed") {
            startRobot shouldBe Coord2(2, 2)
            startWarehouse.size shouldBe 8
            startWarehouse[0].size shouldBe 8
            tests.size shouldBe 15
            tests[0].first shouldBe Movement(-1, 0)
        }
        Then("tests can be executed") {
            val warehouse = startWarehouse
            var robot = startRobot
            var i = 1
            tests.forEach { (testMovement, expectedRobot, expectedWarehouse) ->
                val nextRobot= executeMovement(testMovement, robot, warehouse)
                nextRobot shouldBe expectedRobot
                warehouse shouldBe expectedWarehouse
                robot = nextRobot
                i++
            }
        }
    }
    Given("example input") {
        val (robot, warehouse, movements) = parseWarehouseAndMovements(exampleInputDay15)
        Then("should be parsed") {
            robot shouldBe Coord2(4, 4)
            warehouse.size shouldBe 10
            warehouse[0].size shouldBe 10
            warehouse[1][2] shouldBe '.'
            warehouse[1][3] shouldBe 'O'
            movements.size shouldBe 700
        }
        When("executing robot moves") {
            executeMovements(movements, robot, warehouse)
            Then("should have moved the right boxes") {
                val expectedWarehouse = parseWarehouse("""
                    ##########
                    #.O.O.OOO#
                    #........#
                    #OO......#
                    #OO@.....#
                    #O#.....O#
                    #O.....OO#
                    #O.....OO#
                    #OO....OO#
                    ##########
                """.trimIndent())
                warehouse shouldBe expectedWarehouse
            }
            Then("should have right sum of box gps") {
                sumBoxesGps(warehouse) shouldBe 10092
            }
        }
    }

    Given("exercise input") {
        val (robot, warehouse, movements) = parseWarehouseAndMovements(readResource("inputDay15.txt")!!)
        Then("should be parsed") {
            warehouse.size shouldBe 50
            movements.size shouldBe 20000
        }
        When("executing all movements") {
            executeMovements(movements, robot, warehouse)
            Then("sum should be right") {
                sumBoxesGps(warehouse) shouldBe 1_495_147
            }
        }
    }
} }

typealias Warehouse = List<MutableList<Char>>
typealias Movement = Coord2
typealias Movements = List<Movement>
typealias Robot = Coord2

private fun parseWarehouseAndMovements(input: String): Triple<Robot, Warehouse, Movements> {
    val (warehouseString, movementsStrings) = input.split("\n\n")
    val warehouse = parseWarehouse(warehouseString)
    val movements = movementsStrings.split("\n").joinToString("").map {
        parseMovement(it)
    }
    val robot = findRobot(warehouse)

    return Triple(robot, warehouse, movements)
}

private fun parseMovement(it: Char) = when (it) {
    '<' -> Coord2(-1, 0)
    '>' -> Coord2(1, 0)
    '^' -> Coord2(0, -1)
    'v' -> Coord2(0, 1)
    else -> throw IllegalArgumentException("unexpected movement $it")
}

private fun parseWarehouse(warehouseString: String) = warehouseString.split("\n").map { line ->
    line.toCharArray().toMutableList()
}

private fun findRobot(warehouse: List<List<Char>>) =
    warehouse.indices.flatMap { y ->
        warehouse[y].indices.map { x ->
            Coord2(x, y)
        }
    }.first { warehouse[it.y][it.x] == '@' }

private fun  parseTestInput(input: String): Triple<Robot, Warehouse, List<Triple<Movement, Robot, Warehouse>>> {
    val parts = input.split("\n\n")
    val firstPart = parts.first().split("\n")
    if (firstPart[0] != "Initial state:") throw IllegalArgumentException("Wrong initial state: ${firstPart[0]}")
    val startWarehouse = parseWarehouse(firstPart.drop(1).joinToString("\n"))
    val robot = findRobot(startWarehouse)
    val tests = parts.drop(1).map { part ->
        val partLines = part.split("\n")
        val movementLine = partLines.first()
        if (!movementLine.startsWith("Move")) throw IllegalArgumentException("Wrong movement line $movementLine")
        val movementChar = movementLine.split(" ")[1].first()
        val movement = parseMovement(movementChar)
        val expectedWarehouse = parseWarehouse(partLines.drop(1).joinToString("\n"))
        val expectedRobot = findRobot(expectedWarehouse)
        Triple(movement, expectedRobot, expectedWarehouse)
    }
    return Triple(robot, startWarehouse, tests)
}

private fun executeMovement(movement: Movement, robot: Robot, warehouse: Warehouse): Robot {
    fun moveRobot(robot: Robot, nextRobot: Robot, warehouse: Warehouse) {
        warehouse[robot.y][robot.x] = '.'
        warehouse[nextRobot.y][nextRobot.x] = '@'
    }
    fun boxCanBeMoved(boxCoord: Coord2, movement: Movement, warehouse: Warehouse): Boolean {
        var currCoord = boxCoord + movement
        while(true) {
            if (warehouse[currCoord.y][currCoord.x] == '.') return true
            if (warehouse[currCoord.y][currCoord.x] == '#') return false
            currCoord = currCoord + movement
        }
    }
    fun moveBoxes(boxCoord: Coord2, movement: Movement, warehouse: Warehouse) {
        var currCoord = boxCoord + movement
        while(true) {
            if (warehouse[currCoord.y][currCoord.x] == '.') {
                warehouse[currCoord.y][currCoord.x] = 'O'
                return
            }
            currCoord = currCoord + movement
        }
    }
    val nextRobot = robot + movement
    return if (warehouse[nextRobot.y][nextRobot.x] == '.') {
        moveRobot(robot, nextRobot, warehouse)
        nextRobot
    } else if (warehouse[nextRobot.y][nextRobot.x] == 'O' && boxCanBeMoved(nextRobot, movement, warehouse)) {
        moveBoxes(nextRobot, movement, warehouse)
        moveRobot(robot, nextRobot, warehouse)
        nextRobot
    } else robot
}

private fun executeMovements(movements: Movements, robot: Robot, warehouse: Warehouse): Robot {
    var currRobot = robot
    movements.forEach { movement ->
        currRobot = executeMovement(movement, currRobot, warehouse)
    }
    return robot
}

private fun sumBoxesGps(warehouse: Warehouse) =
    warehouse.indices.flatMap { y ->
        warehouse[y].indices.mapNotNull { x ->
            if (warehouse[y][x] == 'O') y * 100 + x
            else null
        }
    }.sum()
