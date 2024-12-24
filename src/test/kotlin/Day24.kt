
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

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

class FruitMonitoringDevice(gateAndConnections: List<GateAndConnection>) {

    fun executeInput(inputs: List<DeviceInput>): List<Int?> {
        for (input in inputs) {
            val connection = connections[input.first] ?: throw IllegalArgumentException("no input ${input.first}")
            connection.updateValue(input.second)
        }
        val outputs = connections.values.filter { it.tos.isEmpty() }.sortedByDescending { it.id }
        return outputs.map { it.value }
    }

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

class GateConnection(val id: String, var from: DeviceGate? = null, val tos: MutableList<DeviceGate> = mutableListOf(), var value: Int? = null) {
    fun updateValue(v: Int) {
        value = v
        tos.forEach { it.update() }
    }
}
data class DeviceGate(val operation: GateOperation, val inputs: List<GateConnection>, val output: GateConnection) {
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