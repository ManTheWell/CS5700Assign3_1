class ShipmentFactory {
    private val shipmentCreators: Map<String, (List<String>) -> Shipment> = mapOf(
        "standard" to { instructions -> Shipment_Standard(instructions) },
        "express" to { instructions -> Shipment_Express(instructions) },
        "overnight" to { instructions -> Shipment_Overnight(instructions) },
        "bulk" to { instructions -> Shipment_Bulk(instructions) },
    )

    /**
     * MUST follow this pattern:
     * update time, id, "created", type, expected date
     */
    fun createShipment(instructions: List<String>): Shipment? {
        if (instructions.size != 5 || instructions[2] != "created") return null

        val type = instructions[3]

        val creator = shipmentCreators[type.lowercase()]

        if (creator == null) {
            println("Unknown type: $type")
            return null
        }

        return creator(instructions)
    }
}