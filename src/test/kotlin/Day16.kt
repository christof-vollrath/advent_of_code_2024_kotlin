import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

val exampleInputDay16first = """
            ###############
            #.......#....E#
            #.#.###.#.###.#
            #.....#.#...#.#
            #.###.#####.#.#
            #.#.#.......#.#
            #.#.#####.###.#
            #...........#.#
            ###.#.#####.#.#
            #...#.....#.#.#
            #.#.#.###.#.#.#
            #.....#...#.#.#
            #.###.#.#.#.#.#
            #S..#.....#...#
            ###############
        """.trimIndent()

val exampleInputDay16second = """
            #################
            #...#...#...#..E#
            #.#.#.#.#.#.#.#.#
            #.#.#.#...#...#.#
            #.#.#.#.###.#.#.#
            #...#.#.#.....#.#
            #.#.#.#.#.#####.#
            #.#...#.#.#.....#
            #.#.#####.#.###.#
            #.#.#.......#...#
            #.#.###.#####.###
            #.#.#...#.....#.#
            #.#.#.#####.###.#
            #.#.#.........#.#
            #.#.#.#########.#
            #S#.............#
            #################
        """.trimIndent()

class Day16Part1: BehaviorSpec() { init {

    Given("first example input") {
        val (reindeer, reindeerMaze) = parseReindeerMaze(exampleInputDay16first)
        Then("should be parsed") {
            reindeer shouldBe Coord2(1, 13)
            reindeerMaze.size shouldBe 15
            reindeerMaze[0].size shouldBe 15
            reindeerMaze[1][13] shouldBe 'E'
        }
        Then("should be printable") {
            printReindeerMaze(reindeerMaze, ReindeerPath(reindeer, Coord2(0, -1))) shouldBe exampleInputDay16first
        }
        When("finding path") {
            val solution = findReindeerMazePath(reindeer, reindeerMaze)
            Then("should have found right solution") {
                solution.shouldNotBeNull()
                solution.totalCost shouldBe 7036
                printReindeerMaze(reindeerMaze, solution) shouldBe """
                    ###############
                    #.......#....E#
                    #.#.###.#.###^#
                    #.....#.#...#^#
                    #.###.#####.#^#
                    #.#.#.......#^#
                    #.#.#####.###^#
                    #..>>>>>>>>v#^#
                    ###^#.#####v#^#
                    #>>^#.....#v#^#
                    #^#.#.###.#v#^#
                    #^....#...#v#^#
                    #^###.#.#.#v#^#
                    #S..#.....#>>^#
                    ###############
                """.trimIndent()

            }
        }
    }
    Given("second example input") {
        val (reindeer, reindeerMaze) = parseReindeerMaze(exampleInputDay16second)
        Then("should be parsed") {
            reindeer shouldBe Coord2(1, 15)
            reindeerMaze.size shouldBe 17
            reindeerMaze[0].size shouldBe 17
            reindeerMaze[1][15] shouldBe 'E'
        }
        When("finding path") {
            val solution = findReindeerMazePath(reindeer, reindeerMaze)
            Then("should have found right solution") {
                solution.shouldNotBeNull()
                solution.totalCost shouldBe 11048
                printReindeerMaze(reindeerMaze, solution) shouldBe """
                    #################
                    #...#...#...#..E#
                    #.#.#.#.#.#.#.#^#
                    #.#.#.#...#...#^#
                    #.#.#.#.###.#.#^#
                    #>>v#.#.#.....#^#
                    #^#v#.#.#.#####^#
                    #^#v..#.#.#>>>>^#
                    #^#v#####.#^###.#
                    #^#v#..>>>>^#...#
                    #^#v###^#####.###
                    #^#v#>>^#.....#.#
                    #^#v#^#####.###.#
                    #^#v#^........#.#
                    #^#v#^#########.#
                    #S#>>^..........#
                    #################
                """.trimIndent()

            }
        }
    }
    Given("exercise input") {
        val (reindeer, reindeerMaze) = parseReindeerMaze(readResource("inputDay16.txt")!!)
        Then("should be parsed") {
            reindeerMaze.size shouldBe 141
            reindeerMaze[0].size shouldBe 141
        }
        When("finding path") {
            val solution = findReindeerMazePath(reindeer, reindeerMaze)
            Then("should have found right solution") {
                solution.shouldNotBeNull()
                solution.totalCost shouldBe 122492
            }
        }
    }
} }

private fun parseReindeerMaze(input: String): Pair<Coord2, List<List<Char>>> {
    val maze = input.split("\n"). map { line ->
        line.toCharArray().toList()
    }
    val reindeerPos = maze.indices.flatMap { y ->
        maze[y].indices.map { x -> Coord2(x, y)}
    }.filter { p ->
        maze[p.y][p.x] == 'S'
    }.first()
    return reindeerPos to maze
}

private fun nextSteps(pos: Coord2, dir: Coord2, maze: ReindeerMaze): List<ReindeerStep> = sequence {
    val nextPos1 = pos + dir
    if (maze[nextPos1.y][nextPos1.x] in setOf('.', 'E'))
        yield(ReindeerStep(false, nextPos1, dir, 1))
    val turnedRight = turnRight(dir)
    val nextPos2 = pos + turnedRight
    if (maze[nextPos2.y][nextPos2.x] in setOf('.', 'E'))
        yield(ReindeerStep(true, pos, turnedRight, 1000))
    val turnedLeft = turnLeft(dir)
    val nextPos3 = pos + turnedLeft
    if (maze[nextPos3.y][nextPos3.x] in setOf('.', 'E'))
        yield(ReindeerStep(true, pos, turnedLeft, 1000))
}.toList()

private fun turnRight(dir: Coord2) =
    when(dir) {
        Coord2(1, 0) -> Coord2(0, 1)
        Coord2(0, 1) -> Coord2(-1, 0)
        Coord2(-1, 0) -> Coord2(0, -1)
        Coord2(0, -1) -> Coord2(1, 0)
        else -> throw IllegalArgumentException("Can not turn $dir")
    }

private fun turnLeft(dir: Coord2) =
    when(dir) {
        Coord2(1, 0) -> Coord2(0, -1)
        Coord2(0, -1) -> Coord2(-1, 0)
        Coord2(-1, 0) -> Coord2(0, 1)
        Coord2(0, 1) -> Coord2(1, 0)
        else -> throw IllegalArgumentException("Can not turn $dir")
    }

private fun printReindeerMaze(maze: ReindeerMaze, path: ReindeerPath): String {
    val stepsByCoord = path.path.associateBy { it.pos }
    val result = maze.mapIndexed { y, line ->
        line.mapIndexed { x, c->
            val step = stepsByCoord[Coord2(x, y)]
            val p = if (step != null) {
                when(step.dir) {
                    Coord2(1, 0) -> '>'
                    Coord2(0, 1) -> 'v'
                    Coord2(-1, 0) -> '<'
                    Coord2(0, -1) -> '^'
                    else -> throw IllegalArgumentException("Unexpected direction ${step.dir}")
                }
            } else null
            if (c == '.' && p != null) p
            else c // keep S, E
        }.joinToString("")
    }.joinToString("\n")
    return result
}

private typealias ReindeerMaze = List<List<Char>>

private data class ReindeerStep(val rotate: Boolean, val pos: Coord2, val dir: Coord2, val cost: Int)

private class ReindeerPath {
    val path: List<ReindeerStep>
    val totalCost: Int
    val posSet: Set<Pair<Coord2, Coord2>>

    constructor(startPos: Coord2, startDir: Coord2) :
        this(emptyList(), 0, setOf(startPos to startDir))

    internal constructor(path: List<ReindeerStep>, totalCost: Int, posSet: Set<Pair<Coord2, Coord2>>) {
        this.path = path
        this.totalCost = totalCost
        this.posSet = posSet
    }
    fun addStep(step: ReindeerStep): ReindeerPath =
        ReindeerPath(path + step, totalCost + step.cost, posSet + (step.pos to step.dir))
}

private fun findReindeerMazePath(start: Coord2, maze: ReindeerMaze): ReindeerPath? {
    val solutions = mutableListOf<ReindeerPath>()
    val startDir = Coord2(1, 0)
    var currPathes = nextSteps(start, startDir, maze).map { step ->
        ReindeerPath(start, startDir).addStep(step)
    }
    val visited: MutableMap<Pair<Coord2, Coord2>, ReindeerPath> = mutableMapOf<Pair<Coord2, Coord2>, ReindeerPath>()
    while(true) {
        val nextPaths = mutableSetOf<ReindeerPath>()
        currPathes.forEach { path ->
            val currPos = path.path.last().pos
            val currDir = path.path.last().dir
            if (maze[currPos.y][currPos.x] == 'E') solutions.add(path)
            val steps = nextSteps(currPos, currDir, maze)
            steps.forEach { step ->
                if (! path.posSet.contains(step.pos to step.dir))
                    nextPaths.add(path.addStep(step))
            }
        }
        val filteredNextPaths = nextPaths.filter { path: ReindeerPath ->
            val lastStep = path.path.last()
            val alreadyVisited = visited[lastStep.pos to lastStep.dir]
            alreadyVisited == null || alreadyVisited.totalCost > path.totalCost
        }
        if (filteredNextPaths.isEmpty()) break
        filteredNextPaths.forEach { path ->
            val lastStep = path.path.last()
            visited[lastStep.pos to lastStep.dir] = path
        }
        currPathes = filteredNextPaths
    }
    return solutions.minByOrNull { it.totalCost }
}
