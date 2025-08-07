class Shipment_Bulk(data: List<String>) : Shipment(data) {
    private val id: String
    private val type: String

    init {
        id = data[0]
        type = data[3]

        if (data[4].toLong() - data[0].toLong() < 259200)
            note("Expected delivery date less than 3 day expected minimum for bulk shipments")
    }
}
