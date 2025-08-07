class Shipment_Standard(data: List<String>) : Shipment(data) {
    private val id: String
    private val type: String

    init {
        id = data[0]
        type = data[3]
    }
}