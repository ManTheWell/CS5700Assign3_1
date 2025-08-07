class RepositoryManager() {
    private val repository = ShipmentRepository()
    private val shipmentFactory = ShipmentFactory()

    private val operations: Map<String, (List<String>) -> Boolean?> = mapOf(
        "created" to { instructions -> repository.add(shipmentFactory.createShipment(instructions)) },
        "shipped" to { data -> getShipment(data[1])?.shipped(data) },
        "location" to { data -> getShipment(data[1])?.location(data) },
        "delivered" to { data -> getShipment(data[1])?.delivered(data) },
        "delayed" to { data -> getShipment(data[1])?.delayed(data) },
        "lost" to { data -> getShipment(data[1])?.lost(data) },
        "canceled" to { data -> getShipment(data[1])?.canceled(data) },
        "note" to { data -> getShipment(data[1])?.note(data) }
    )

    fun processInput(input: String) : Boolean? {
        val data = input.split(",")

        val op = data[2]
        val operation = operations[op.lowercase()]

        if (operation == null) {
            println("Unknown operation: $op")
            return false
        }

        return operation(data)
    }

    fun getShipment(id: String): Shipment? {
        return repository.getShipment(id)
    }
}