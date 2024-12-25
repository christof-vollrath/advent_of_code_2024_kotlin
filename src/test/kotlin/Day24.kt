
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.math.pow

val exampleInputDay24 = """
            x00: 1
            x01: 1
            x02: 1
            y00: 0
            y01: 1
            y02: 0
            
            x00 AND y00 -> z00
            x01 XOR y01 -> z01
            x02 OR y02 -> z02
        """.trimIndent()
val largerExampleInputDay24 = """
            x00: 1
            x01: 0
            x02: 1
            x03: 1
            x04: 0
            y00: 1
            y01: 1
            y02: 1
            y03: 1
            y04: 1
            
            ntg XOR fgs -> mjb
            y02 OR x01 -> tnw
            kwq OR kpj -> z05
            x00 OR x03 -> fst
            tgd XOR rvg -> z01
            vdt OR tnw -> bfw
            bfw AND frj -> z10
            ffh OR nrd -> bqk
            y00 AND y03 -> djm
            y03 OR y00 -> psh
            bqk OR frj -> z08
            tnw OR fst -> frj
            gnj AND tgd -> z11
            bfw XOR mjb -> z00
            x03 OR x00 -> vdt
            gnj AND wpb -> z02
            x04 AND y00 -> kjc
            djm OR pbm -> qhw
            nrd AND vdt -> hwm
            kjc AND fst -> rvg
            y04 OR y02 -> fgs
            y01 AND x02 -> pbm
            ntg OR kjc -> kwq
            psh XOR fgs -> tgd
            qhw XOR tgd -> z09
            pbm OR djm -> kpj
            x03 XOR y03 -> ffh
            x00 XOR y04 -> ntg
            bfw OR bqk -> z06
            nrd XOR fgs -> wpb
            frj XOR qhw -> z04
            bqk OR frj -> z07
            y03 OR x01 -> nrd
            hwm AND bqk -> z03
            tgd XOR rvg -> z12
            tnw OR pbm -> gnj
        """.trimIndent()

class Day24Part1: BehaviorSpec() { init {

    Given("example") {
        val (input, connections) = parseDay24Input(exampleInputDay24)
        Then("connections should be parsed") {
            input.size shouldBe 6
            connections.size shouldBe 3
        }
        Given("device") {
            val device = FruitMonitoringDevice(connections)
            Then("device should be constructed") {
                device.gates.size shouldBe 3
                device.connections.size shouldBe 9
            }
            When("executing input") {
                val output = device.executeInput(input)
                Then("output should be right") {
                    output shouldBe listOf(1, 0 , 0)
                    output.joinToString("").toInt(2) shouldBe 4
                }
            }
        }
    }
    Given("larger example") {
        val (input, connections) = parseDay24Input(largerExampleInputDay24)
        Then("connections should be parsed") {
            input.size shouldBe 10
            connections.size shouldBe 36
        }
        Given("device") {
            val device = FruitMonitoringDevice(connections)
            Then("device should be constructed") {
                device.gates.size shouldBe 36
                device.connections.size shouldBe 46
                val output = device.connections.values.filter { it.tos.isEmpty() }
                output.size shouldBe 13
            }
            When("executing input") {
                val output = device.executeInput(input)
                Then("output should be right") {
                    output shouldBe listOf(0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0)
                    output.joinToString("").toInt(2) shouldBe 2024
                }
            }
        }
    }

    Given("exercise input") {
        val (input, connections) = parseDay24Input(readResource("inputDay24.txt")!!)
        Then("should be parsed") {
            input.size shouldBe 90
            connections.size shouldBe 222
        }
        val device = FruitMonitoringDevice(connections)
        When("executing input") {
            val output = device.executeInput(input)
            Then("output should be right") {
                output.joinToString("").toLong(2) shouldBe 53190357879014L
            }
        }
    }
}}

class Day24Part2: BehaviorSpec() { init {

    Given("exercise input") {
        val (input, connections) = parseDay24Input(readResource("inputDay24.txt")!!)
        Then("should be parsed") {
            input.filter { it.first.startsWith("x")}.size shouldBe 45
            input.filter { it.first.startsWith("y")}.size  shouldBe 45
            connections.size shouldBe 222
        }
        val device = FruitMonitoringDevice(connections)
        Then("device should have right inputs") {
            device.inputXIds.size shouldBe 45
            device.inputYIds.size shouldBe 45
        }
        Then("creating input pairs") {
            convertToInput("x", 1, 4) shouldBe listOf("x00" to 1, "x01" to 0, "x02" to 0, "x03" to 0)
            convertToInput("x", 2, 4) shouldBe listOf("x00" to 0, "x01" to 1, "x02" to 0, "x03" to 0)
            convertToInput("x", 7, 4) shouldBe listOf("x00" to 1, "x01" to 1, "x02" to 1, "x03" to 0)
        }
        // To work as an adder connections must form the right adder circuit see https://en.wikipedia.org/wiki/Adder_(electronics)
        Then("check X00, Y00 to fom an adder, X00 xor Y00 -> Z00") {
            val connectedXorGates = device.connections["x00"]?.tos?.filter { it.operation == GateOperation.XOR }
            connectedXorGates.shouldNotBeNull()
            connectedXorGates.size shouldBe 1
            connectedXorGates.first().inputs.map { it.id } shouldBe listOf("x00", "y00")
            connectedXorGates.first().output.id shouldBe "z00"
            val connectedAndGates = device.connections["x00"]?.tos?.filter { it.operation == GateOperation.AND }
            connectedAndGates.shouldNotBeNull()
            connectedAndGates.size shouldBe 1
            connectedAndGates.first().inputs.map { it.id }.toSet() shouldBe setOf("x00", "y00")
            connectedAndGates.first().output.id shouldBe "jfs" // carrier
        }
        Then("check X01, Y01 to form a full adder") {
            val connectedXorGates1 = device.connections["x01"]?.tos?.filter { it.operation == GateOperation.XOR }
            connectedXorGates1.shouldNotBeNull()
            connectedXorGates1.size shouldBe 1
            connectedXorGates1.first().inputs.map { it.id }.toSet() shouldBe setOf("x01", "y01")
            val xor1OutId = connectedXorGates1.first().output.id
            val connectedXorGates2 = device.connections[xor1OutId]?.tos?.filter { it.operation == GateOperation.XOR }
            connectedXorGates2.shouldNotBeNull()
            connectedXorGates2.size shouldBe 1
            connectedXorGates2.first().inputs.map { it.id }.toSet() shouldBe setOf(xor1OutId, "jfs") // carrier from x00, Y00
            val connectedAndGates1 = device.connections["x01"]?.tos?.filter { it.operation == GateOperation.AND }
            connectedAndGates1.shouldNotBeNull()
            connectedAndGates1.size shouldBe 1
            connectedAndGates1.first().inputs.map { it.id }.toSet() shouldBe setOf("x01", "y01")
            val and1OutId = connectedAndGates1.first().output.id
            val connectedAndGates2 = device.connections[xor1OutId]?.tos?.filter { it.operation == GateOperation.AND }
            connectedAndGates2.shouldNotBeNull()
            connectedAndGates2.size shouldBe 1
            connectedAndGates2.first().inputs.map { it.id }.toSet() shouldBe setOf(xor1OutId, "jfs")
            val and2OutId = connectedAndGates2.first().output.id
            val connectedOrGates = device.connections[and1OutId]?.tos?.filter { it.operation == GateOperation.OR }
            connectedOrGates.shouldNotBeNull()
            connectedOrGates.size shouldBe 1
            connectedOrGates.first().inputs.map { it.id }.toSet() shouldBe setOf(and1OutId, and2OutId)
        }
        Then("check X01, Y01 and X02, Y02 to form full adders") {
            val carry2 = checkFullAdder(1, "jfs", device)
            checkFullAdder(2, carry2, device)
        }
        Then("check all full adders") {
            val repairedConnections = repairConnections(connections, listOf(
                "z09" to "hnd", "tdv" to "z16", "bks" to "z23", "tjp" to "nrn"
            ))
            val repairedDevice = FruitMonitoringDevice(repairedConnections)
            var carry = "jfs"
            for (i in 1 .. 44) {
                carry = checkFullAdder(i, carry, repairedDevice)
            }
        }
        When("testing device with some cases") {
            testDevice(device, 1L, 1L) shouldBe 2L
            testDevice(device, 2L, 2L) shouldBe 4L
            testDevice(device, 4L, 0L) shouldBe 4L
            testDevice(device, 16L, 4L) shouldBe 20L
            val input1 = "111111111111111111111111111111111111111".toLong(2)
            testDevice(device, input1, 0L) shouldNotBe  input1 // Bug!
            testDevice(device, 0L, input1) shouldNotBe input1 // Bug!
            val output = "1000000000000000000000000000000000000000".toLong(2)
            testDevice(device, input1, 1L) shouldNotBe output // Bug!
            testDevice(device, 1L, input1) shouldNotBe output // Bug!
            val maxValueOfDevice = (2.0).pow(44).toLong()-1
            testDevice(device, maxValueOfDevice, 0L).toString(2) shouldNotBe maxValueOfDevice.toString(2) // Bug!
            testDevice(device, 0L, maxValueOfDevice).toString(2) shouldNotBe maxValueOfDevice.toString(2) // Bug!
        }
        When("testing repaired device with some cases") {
            val repairedConnections = repairConnections(connections, listOf(
                "z09" to "hnd", "tdv" to "z16", "bks" to "z23", "tjp" to "nrn"
            ))
            val repairedDevice = FruitMonitoringDevice(repairedConnections)
            testDevice(repairedDevice, 1L, 1L) shouldBe 2L
            testDevice(repairedDevice, 2L, 2L) shouldBe 4L
            testDevice(repairedDevice, 4L, 0L) shouldBe 4L
            testDevice(repairedDevice, 16L, 4L) shouldBe 20L
            val input1 = "111111111111111111111111111111111111111".toLong(2)
            testDevice(repairedDevice, input1, 0L) shouldBe input1
            testDevice(repairedDevice, 0L, input1) shouldBe input1
            val output = "1000000000000000000000000000000000000000".toLong(2)
            testDevice(repairedDevice, input1, 1L) shouldBe output
            testDevice(repairedDevice, 1L, input1) shouldBe output
            val maxValueOfDevice = (2.0).pow(44).toLong()-1
            testDevice(repairedDevice, maxValueOfDevice, 0L).toString(2) shouldBe maxValueOfDevice.toString(2)
            testDevice(repairedDevice, 0L, maxValueOfDevice).toString(2) shouldBe maxValueOfDevice.toString(2)
        }
        Then("flattening repairs") {
            flattenRepairs(listOf(
                "z09" to "hnd", "tdv" to "z16", "bks" to "z23", "tjp" to "nrn"
            )).joinToString(",") shouldBe "bks,hnd,nrn,tdv,tjp,z09,z16,z23"
        }
    }
}}

typealias DeviceInput = Pair<String, Int>
enum class GateOperation { AND, OR, XOR}
data class GateAndConnection(val inputs: List<String>, val operation: GateOperation, val output: String)

private fun parseDay24Input(inputString: String): Pair<List<DeviceInput>, List<GateAndConnection>> {
    val (deviceInputString, gateConnectionString) = inputString.split("\n\n")
    val deviceInputs = deviceInputString.split("\n"). map { line ->
        val (input, valueStr) = line.split(":").map { it.trim() }
        input to valueStr.toInt()
    }
    val gateAndConnections = gateConnectionString.split("\n"). map { line ->
        val (input1, operationString, input2, _, output) = line.split(" ")
        val operation = GateOperation.valueOf(operationString)
        GateAndConnection(listOf(input1, input2), operation, output)
    }
    return deviceInputs to gateAndConnections
}

private class FruitMonitoringDevice(gateAndConnections: List<GateAndConnection>) {

    fun executeInput(inputs: List<DeviceInput>): List<Int?> {
        for (input in inputs) {
            val connection = connections[input.first] ?: throw IllegalArgumentException("no input ${input.first}")
            connection.updateValue(input.second)
        }
        val outputs = connections.values.filter { it.tos.isEmpty() }.sortedByDescending { it.id }
        val result = outputs.map { it.value }
        reset()
        return result
    }
    fun reset() {
        for (connection in connections)
            connection.value.value = null
    }
    val inputXIds: List<String>
        get() = connections.values.filter { it.from == null && it.id.startsWith("x")}.map { it.id }.sortedDescending()
    val inputYIds: List<String>
        get() = connections.values.filter { it.from == null && it.id.startsWith("y")}.map { it.id }.sortedDescending()

    val connections = mutableMapOf<String, GateConnection>()
    val gates = mutableListOf<DeviceGate>()

    init {
        for (gateAndConnection in gateAndConnections) {
            val inputConnections = gateAndConnection.inputs.map { id ->
                var inputConnection = connections[id]
                if (inputConnection == null) {
                    inputConnection = GateConnection(id)
                    connections[id] = inputConnection
                }
                inputConnection
            }
            var outputConnection = connections[gateAndConnection.output]
            if (outputConnection == null) {
                outputConnection = GateConnection(gateAndConnection.output)
                connections[gateAndConnection.output] = outputConnection
            }
            val gate = DeviceGate(gateAndConnection.operation, inputConnections, outputConnection)
            for (inputConnection in inputConnections) inputConnection.tos.add(gate)
            outputConnection.from = gate
            gates.add(gate)
        }
    }
}

private class GateConnection(val id: String, var from: DeviceGate? = null, val tos: MutableList<DeviceGate> = mutableListOf(), var value: Int? = null) {
    fun updateValue(v: Int) {
        value = v
        tos.forEach { it.update() }
    }
}
private data class DeviceGate(val operation: GateOperation, val inputs: List<GateConnection>, val output: GateConnection) {
    fun update() {
        if (inputs.all { it.value != null}) {
            val result = when(operation) {
                GateOperation.OR -> inputs.any { it.value == 1 }
                GateOperation.AND -> inputs.all { it.value == 1 }
                GateOperation.XOR -> inputs.count {it.value == 1} % 2 != 0 // xor for multiple arguments -> count of true inputs is odd
            }
            output.updateValue(if (result) 1 else 0)
        }
    }
}

private fun testDevice(device: FruitMonitoringDevice, x: Long, y: Long): Long {
    val inputX = convertToInput("x", x, device.inputXIds.size)
    val inputY = convertToInput("y", y, device.inputYIds.size)

    val output = device.executeInput(inputX + inputY)
    return output.joinToString("").toLong(2)
}

private fun convertToInput(prefix: String, n: Long, bitSize: Int): List<Pair<String, Int>> {
    val string = n.toString(2).reversed()
    val inputPairs = (0 until bitSize).map {
        formatInputId(prefix, it) to if (it < string.length) string[it].toString().toInt()
        else 0
    }
    return inputPairs
}

fun formatInputId(prefix: String, nr: Int) =
    prefix + "%02d".format(nr)


private fun checkFullAdder(n: Int, carryId: String, device: FruitMonitoringDevice): String {
    val xId = formatInputId("x", n)
    val yId = formatInputId("y", n)
    val connectedXorGates1 = device.connections[xId]?.tos?.filter { it.operation == GateOperation.XOR }
    connectedXorGates1.shouldNotBeNull()
    connectedXorGates1.size shouldBe 1
    connectedXorGates1.first().inputs.map { it.id }.toSet() shouldBe setOf(xId, yId)
    val xor1OutId = connectedXorGates1.first().output.id
    val connectedXorGates2 = device.connections[xor1OutId]?.tos?.filter { it.operation == GateOperation.XOR }
    connectedXorGates2.shouldNotBeNull()
    if (connectedXorGates2.size != 1) println("no xor gate found for xor $xor1OutId from $xId $yId carry $carryId")
    connectedXorGates2.size shouldBe 1
    connectedXorGates2.first().inputs.map { it.id }.toSet() shouldBe setOf(xor1OutId, carryId) // carrier from x00, Y00
    val connectedAndGates1 = device.connections[xId]?.tos?.filter { it.operation == GateOperation.AND }
    connectedAndGates1.shouldNotBeNull()
    connectedAndGates1.size shouldBe 1
    connectedAndGates1.first().inputs.map { it.id }.toSet() shouldBe setOf(xId, yId)
    val and1OutId = connectedAndGates1.first().output.id
    val connectedAndGates2 = device.connections[xor1OutId]?.tos?.filter { it.operation == GateOperation.AND }
    connectedAndGates2.shouldNotBeNull()
    connectedAndGates2.size shouldBe 1
    connectedAndGates2.first().inputs.map { it.id }.toSet() shouldBe setOf(xor1OutId, carryId)
    val and2OutId = connectedAndGates2.first().output.id
    val connectedOrGates = device.connections[and1OutId]?.tos?.filter { it.operation == GateOperation.OR }
    connectedOrGates.shouldNotBeNull()
    if (connectedOrGates.size != 1) println("no or gate found for and $and1OutId from $xId $yId carry $carryId")
    connectedOrGates.size shouldBe 1
    connectedOrGates.first().inputs.map { it.id }.toSet() shouldBe setOf(and1OutId, and2OutId)
    return connectedOrGates.first().output.id
}

private fun repairConnections(connections: List<GateAndConnection>, exchange: Pair<String, String>): List<GateAndConnection> =
    connections.map { connection ->
        if (connection.output == exchange.first)
            connection.copy(output = exchange.second)
        else if (connection.output == exchange.second)
            connection.copy(output = exchange.first)
        else connection
    }

private fun repairConnections(connections: List<GateAndConnection>, exchanges: List<Pair<String, String>>): List<GateAndConnection> {
    var repairedConnections = connections
    for (repair in exchanges)
        repairedConnections = repairConnections(repairedConnections, repair)
    return repairedConnections
}

private fun flattenRepairs(repairs: List<Pair<String, String>>) =
    repairs.flatMap { it.toList() }.sorted()
