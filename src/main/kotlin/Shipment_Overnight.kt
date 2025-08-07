class Shipment_Overnight(data: List<String>) : Shipment(data) {
    private val id: String
    private val type: String

    init {
        id = data[0]
        type = data[3]

        if (data[4].toLong() - data[0].toLong() > 86400)
            note("Expected delivery date greater than 1 day expected maximum for overnight shipments")
    }
}