import kotlin.math.abs

fun readResource(name: String) = ClassLoader.getSystemClassLoader().getResource(name)?.readText()

fun <E> List<E>.permute():List<List<E>> {
    if (size == 1) return listOf(this)
    val perms = mutableListOf<List<E>>()
    val sub = get(0)
    for(perm in drop(1).permute())
        for (i in 0..perm.size){
            val newPerm=perm.toMutableList()
            newPerm.add(i, sub)
            perms.add(newPerm)
        }
    return perms
}

data class Coord2(val x: Int, val y: Int) {
    infix fun manhattanDistance(other: Coord2): Int = abs(x - other.x) + abs(y - other.y)
    operator fun plus(direction: Coord2) = Coord2(x + direction.x, y + direction.y)
    operator fun minus(direction: Coord2) = Coord2(x - direction.x, y - direction.y)
    operator fun times(n: Int) = Coord2(x * n, y * n)
    operator fun times(matrix: List<List<Int>>) =
        Coord2(x * matrix[0][0] + y * matrix[0][1],
            x * matrix[1][0] + y * matrix[1][1])
    fun neighbors() = neighborOffsets.map { neighborOffset ->
        this + neighborOffset
    }
    fun neighbors8() = neighbor8Offsets.map { neighborOffset ->
        this + neighborOffset
    }

    companion object {
        val neighborOffsets = listOf(Coord2(-1, 0), Coord2(1, 0), Coord2(0, -1), Coord2(0, 1))
        val neighbor8Offsets = (-1..1).flatMap { y ->
            (-1..1).mapNotNull { x ->
                if (x != 0 || y != 0) Coord2(x, y)
                else null
            }
        }
        val turnMatrixLeft = listOf(
            listOf(0, 1),
            listOf(-1, 0)
        )
        val turnMatrixRight = listOf(
            listOf(0, -1),
            listOf(1, 0)
        )
    }
}
typealias Plane<E>  = List<List<E>>
operator fun <E> Plane<E>.get(coord: Coord2) = this[coord.y][coord.x]

fun lcm(a: Long, b: Long) = abs(a * b) / gcd(a, b)
fun lcm(numbers: List<Long>) = numbers.drop(1).fold(numbers[0]) { acc, curr ->
    lcm(acc, curr)
}

tailrec fun gcd(a: Long, b: Long): Long =
    when {
        a == 0L -> b
        b == 0L -> a
        a > b -> gcd(a-b, b)
        else -> gcd(a, b-a)
    }

