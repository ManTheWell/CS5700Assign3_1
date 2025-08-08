import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShipmentTest {
    private val baseTime = "1690000000000" // Corresponds to 07/28/2023 at 21:06
    private val baseData = listOf(baseTime, "ABC123", "", "Standard", "1690600000000")

    @Test
    fun `test add and get to repository`() {
        val repository = ShipmentRepository()

        assertEquals(false, repository.add(null))

        val ship1 = Shipment(listOf(baseTime, "1", "created", "Standard", "1690600000000"))
        val ship2 = Shipment(listOf(baseTime, "2", "created", "Standard", "1690600000000"))

        assertEquals(true, repository.add(ship1))
        assertEquals(true, repository.add(ship2))
        assertEquals(false, repository.add(null))

        assertNotNull(repository.getShipment(ship1.getID()))
        assertNotNull(repository.getShipment(ship2.getID()))
        assertNull(repository.getShipment("DOES NOT EXIST"))
    }

    @Test
    fun `create standard shipment`() {
        val shipment = ShipmentFactory().createShipment(listOf(baseTime, "ABC123", "created", "Standard", "1690600000000"))
        val info = shipment!!.getData()
        assertEquals("ABC123", info.id)
        assertEquals("Standard", info.type)
        assertEquals("Created", info.status)
        assertEquals("Origin Warehouse", info.location)
        assertEquals("07/28/2023 at 21:06", info.expectedDelivery)
        assertTrue(info.updates[0].startsWith("Created on "))
    }

    @Test
    fun `create normal time bulk shipment`() {
        val shipment = ShipmentFactory().createShipment(listOf("1690000000000", "ABC123", "created", "Bulk", "2690000000000"))
        val info = shipment!!.getData()
        assertEquals("ABC123", info.id)
        assertTrue(info.notes.isEmpty())
    }

    @Test
    fun `create short time bulk shipment`() {
        val shipment = ShipmentFactory().createShipment(listOf("1690000000000", "ABC123", "created", "Bulk", "1690000000000"))
        val info = shipment!!.getData()
        assertEquals("ABC123", info.id)
        assertTrue(info.notes[0] == "Expected delivery date less than 3 day expected minimum for bulk shipments")
    }

    @Test
    fun `create on time express shipment`() {
        val shipment = ShipmentFactory().createShipment(listOf("1690000000000", "ABC123", "created", "Express", "1690000000000"))
        val info = shipment!!.getData()
        assertEquals("ABC123", info.id)
        assertTrue(info.notes.isEmpty())
    }

    @Test
    fun `create overdue express shipment`() {
        val shipment = ShipmentFactory().createShipment(listOf("1690000000000", "ABC123", "created", "Express", "2690000000000"))
        val info = shipment!!.getData()
        assertEquals("ABC123", info.id)
        assertTrue(info.notes[0] == "Expected delivery date greater than 3 day expected maximum for express shipments")
    }

    @Test
    fun `create on time overnight shipment`() {
        val shipment = ShipmentFactory().createShipment(listOf("1690000000000", "ABC123", "created", "overnight", "1690000000000"))
        val info = shipment!!.getData()
        assertEquals("ABC123", info.id)
        assertTrue(info.notes.isEmpty())
    }

    @Test
    fun `create overdue overnight shipment`() {
        val shipment = ShipmentFactory().createShipment(listOf("1690000000000", "ABC123", "created", "overnight", "2690000000000"))
        val info = shipment!!.getData()
        assertEquals("ABC123", info.id)
        assertTrue(info.notes[0] == "Expected delivery date greater than 1 day expected maximum for overnight shipments")
    }

    @Test
    fun `should initialize shipment correctly`() {
        val info = Shipment(baseData).getData()
        assertEquals("ABC123", info.id)
        assertEquals("Standard", info.type)
        assertEquals("Created", info.status)
        assertEquals("Origin Warehouse", info.location)
        assertEquals("07/28/2023 at 21:06", info.expectedDelivery)
        assertTrue(info.updates[0].startsWith("Created on "))
    }

    @Test
    fun `should update status to shipped`() {
        // 1690700000000 corresponds to 07/30/2023 at 00:53
        val shipData = listOf("x", "1690100000000", "", "1690700000000")
        val shipment = Shipment(baseData)
        shipment.shipped(shipData)
        val info = shipment.getData()
        assertEquals("Shipped", info.status)
        assertEquals("07/30/2023 at 00:53", info.expectedDelivery)
        assertTrue(info.updates[0].contains("Shipped on"))
    }

    @Test
    fun `should update location`() {
        val locationData = listOf("x", "1690150000000", "", "Chicago")
        val shipment = Shipment(baseData)
        shipment.location(locationData)
        val info = shipment.getData()
        assertEquals("Arrived at New Location", info.status)
        assertEquals("Chicago", info.location)
        assertTrue(info.updates[0].contains("New shipment location: Chicago"))
    }

    @Test
    fun `should update to delivered`() {
        val deliveryData = listOf("x", "1690200000000")
        val shipment = Shipment(baseData)
        shipment.delivered(deliveryData)
        val info = shipment.getData()
        assertEquals("Delivered", info.status)
        assertEquals("Delivered", info.location)
        assertEquals("Delivery Complete", info.expectedDelivery)
        assertTrue(info.updates[0].contains("Delivered at"))
    }

    @Test
    fun `should update to delayed`() {
        val delayData = listOf("x", "1690250000000", "", "1690750000000")
        val shipment = Shipment(baseData)
        shipment.delayed(delayData)
        val info = shipment.getData()
        assertEquals("Delayed", info.status)
        assertTrue(info.expectedDelivery.contains("/")) // formatted date
        assertTrue(info.updates[0].contains("Delayed on"))
    }

    @Test
    fun `should update to lost`() {
        val lostData = listOf("x", "1690300000000")
        val shipment = Shipment(baseData)
        shipment.lost(lostData)
        val info = shipment.getData()
        assertEquals("Lost", info.status)
        assertEquals("Unknown", info.location)
        assertEquals("N/A", info.expectedDelivery)
        assertTrue(info.updates[0].contains("Lost on"))
    }

    @Test
    fun `should update to canceled`() {
        val cancelData = listOf("x", "1690350000000")
        val shipment = Shipment(baseData)
        shipment.canceled(cancelData)
        val info = shipment.getData()
        assertEquals("Canceled", info.status)
        assertEquals("N/A", info.location)
        assertEquals("N/A", info.expectedDelivery)
        assertTrue(info.updates[0].contains("Canceled on"))
    }

    @Test
    fun `should add note`() {
        val noteData = listOf("x", "1690400000000", "", "Fragile - handle with care")
        val shipment = Shipment(baseData)
        shipment.note(noteData)
        val info = shipment.getData()
        assertEquals("Fragile - handle with care", info.notes[0])
    }

    @Test
    fun `should handle bad timestamps gracefully`() {
        val badData = listOf("x", "invalid_timestamp", "", "UnknownPlace")
        val shipment = Shipment(baseData)
        shipment.location(badData)
        val info = shipment.getData()
        assertEquals("UnknownPlace", info.location)
        assertTrue(info.updates[0].contains("on "))
    }
}
