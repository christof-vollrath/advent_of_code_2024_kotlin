import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class Day12Part1: BehaviorSpec() { init {

    Given("example1") {
        val example1 = parseGardenMap("""
                AAAA
                BBCD
                BBCC
                EEEC
            """.trimIndent())
        Then("garden map should be parsed") {
            example1[0].size shouldBe 4
            example1.size shouldBe 4
            example1[3][3] shouldBe 'C'
        }
        When("finding regions 1") {
            val regions = findRegions(example1)
            Then("regions should be found") {
                regions.size shouldBe 5
                regions[0].crop shouldBe 'A'
                regions[0].plots shouldBe listOf(Plot(x=0, y=0), Plot(x=1, y=0), Plot(x=2, y=0), Plot(x=3, y=0))
                regions.map { it.area() } shouldBe listOf(4, 4, 4, 1, 3)
                regions.map { it.perimeter() } shouldBe listOf(10, 8, 10, 4, 8)
            }
        }
        val example2 = parseGardenMap("""
                OOOOO
                OXOXO
                OOOOO
                OXOXO
                OOOOO
            """.trimIndent())
        When("finding regions 2") {
            val regions = findRegions(example2)
            Then("regions should be found") {
                regions.size shouldBe 5
                regions[0].crop shouldBe 'O'
                regions[1].crop shouldBe 'X'
                regions.map { it.area() } shouldBe listOf(21, 1, 1, 1, 1)
                regions.map { it.perimeter() } shouldBe listOf(36, 4, 4, 4, 4)
            }
        }
        val example3 = parseGardenMap("""
                RRRRIICCFF
                RRRRIICCCF
                VVRRRCCFFF
                VVRCCCJFFF
                VVVVCJJCFE
                VVIVCCJJEE
                VVIIICJJEE
                MIIIIIJJEE
                MIIISIJEEE
                MMMISSJEEE
            """.trimIndent())
        When("finding regions 3") {
            val regions = findRegions(example3)
            Then("regions should be found") {
                regions.size shouldBe 11
                regions[0].crop shouldBe 'R'
                regions[1].crop shouldBe 'I'
                regions.map { it.area() } shouldBe listOf(12, 4, 14, 10, 13, 11, 1, 13, 14, 5, 3)
                regions.map { it.perimeter() } shouldBe listOf(18, 8, 28, 18, 20, 20, 4, 18, 22, 12, 8)
            }
            calculateFencePrice(regions) shouldBe 1930
        }
    }
    Given("exercise input") {
        val regions = findRegions(parseGardenMap(readResource("inputDay12.txt")!!))
        val result = calculateFencePrice(regions)
        Then("should calculate the right price") {
            result shouldBe 1_549_354
        }
    }
} }

typealias Crop = Char
typealias GardenMap = List<List<Crop>>
typealias Plot = Coord2

fun parseGardenMap(input: String): GardenMap = input.split("\n").map { line ->
    line.map { it }
}

fun findRegions(gardenMap: GardenMap): List<GardenRegion> {

    val regionsByPlot = mutableMapOf<Plot, GardenRegion>()
    val result = mutableMapOf<UUID, GardenRegion>()

    fun mergeRegion(from: GardenRegion, to: GardenRegion) {
        from.plots.toList().forEach { plot ->
            regionsByPlot.remove(plot)
            to.plots.add(plot)
            regionsByPlot[plot] = to
        }
        result.remove(from.id)
    }

    for (y in gardenMap.indices)
        for (x in gardenMap[y].indices) {
            val currentCrop = gardenMap[y][x]
            if (x >= 1) {
                val leftCrop = gardenMap[y][x-1]
                if (currentCrop == leftCrop) {
                    val currentRegion = regionsByPlot[Plot(x-1, y)]!!
                    currentRegion.plots.add(Plot(x, y))
                    regionsByPlot[Plot(x, y)] = currentRegion
                    if (y >= 1 && currentCrop == gardenMap[y-1][x]) {
                        // merge current region with the region above
                        val regionAbove = regionsByPlot[Plot(x, y-1)]!!
                        if (regionAbove.id != currentRegion.id)
                            mergeRegion(currentRegion, regionAbove)
                    }
                    continue
                }
            }
            if (y >= 1) {
                val upperCrop = gardenMap[y-1][x]
                if (currentCrop == upperCrop) {
                    val currentRegion = regionsByPlot[Plot(x, y-1)]!!
                    currentRegion.plots.add(Plot(x, y))
                    regionsByPlot[Plot(x, y)] = currentRegion
                    continue
                }
            }
            // no region to extend
            val currentRegion = GardenRegion(currentCrop)
            currentRegion.plots.add(Plot(x, y))
            regionsByPlot[Plot(x, y)] = currentRegion
            result[currentRegion.id] =  currentRegion
        }

    return result.values.toList()
}

data class GardenRegion(val crop: Crop, var plots: MutableList<Plot> = mutableListOf(), val id: UUID = UUID.randomUUID()) {
    fun area() = plots.size
    fun perimeter(): Int {
        val neighborsDelta = listOf(Coord2(1, 0), Coord2(0, 1), Coord2(-1, 0), Coord2(0, -1))
        val plotSet = plots.toSet()
        return plots.map { plot ->
            val neighbors = neighborsDelta.map { delta ->
                plot + delta
            }
            val neighborsCount = neighbors.filter { plotSet.contains(it) }.count()
            4 - neighborsCount // returning edges withoud neighbor
        }.sum()
    }
}

fun calculateFencePrice(regions: List<GardenRegion>) = regions.map { region ->
    region.area() * region.perimeter()
}.sum()
