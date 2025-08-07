class ShipmentRepository {
    private val shipments = mutableMapOf<String, Shipment>()

    fun add(shipment: Shipment?) : Boolean {
        if (shipment == null) return false

        shipments[shipment.getID()] = shipment
        return true
    }

    fun getShipment(id: String): Shipment? {
        return shipments[id]
    }
}
