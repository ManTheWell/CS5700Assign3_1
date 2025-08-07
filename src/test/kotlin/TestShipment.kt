import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ShipmentTest {
    private lateinit var shipment: Shipment
    private val baseTime = "1690000000000" // Corresponds to some readable date
    private val baseData = listOf(baseTime, "ABC123", "", "Standard", "1690600000000")

    @BeforeEach
    fun setUp() {
        shipment = Shipment(baseData)
    }

    @Test
    fun `should initialize shipment correctly`() {
        val info = shipment.getData()
        assertEquals("ABC123", info.id)
        assertEquals("Standard", info.type)
        assertEquals("Created", info.status)
        assertEquals("Origin Warehouse", info.location)
        assertEquals("1690600000000", info.expectedDelivery)
        assertTrue(info.updates[0].startsWith("Created on "))
    }

    @Test
    fun `should update status to shipped`() {
        val shipData = listOf("x", "1690100000000", "", "1690700000000")
        shipment.shipped(shipData)
        val info = shipment.getData()
        assertEquals("Shipped", info.status)
        assertEquals("1690700000000", info.expectedDelivery)
        assertTrue(info.updates[0].contains("Shipped on"))
    }

    @Test
    fun `should update location`() {
        val locationData = listOf("x", "1690150000000", "", "Chicago")
        shipment.location(locationData)
        val info = shipment.getData()
        assertEquals("Arrived at New Location", info.status)
        assertEquals("Chicago", info.location)
        assertTrue(info.updates[0].contains("New shipment location: Chicago"))
    }

    @Test
    fun `should update to delivered`() {
        val deliveryData = listOf("x", "1690200000000")
        shipment.delivered(deliveryData)
        val info = shipment.getData()
        assertEquals("Delivered", info.status)
        assertEquals("Delivered", info.location)
        assertEquals("N/A", info.expectedDelivery)
        assertTrue(info.updates[0].contains("Delivered at"))
    }

    @Test
    fun `should update to delayed`() {
        val delayData = listOf("x", "1690250000000", "", "1690750000000")
        shipment.delayed(delayData)
        val info = shipment.getData()
        assertEquals("Delayed", info.status)
        assertTrue(info.expectedDelivery.contains("/")) // formatted date
        assertTrue(info.updates[0].contains("Delayed on"))
    }

    @Test
    fun `should update to lost`() {
        val lostData = listOf("x", "1690300000000")
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
        shipment.note(noteData)
        val info = shipment.getData()
        assertEquals("Fragile - handle with care", info.notes[0])
    }

    @Test
    fun `should handle bad timestamps gracefully`() {
        val badData = listOf("x", "invalid_timestamp", "", "UnknownPlace")
        shipment.location(badData)
        val info = shipment.getData()
        assertEquals("UnknownPlace", info.location)
        assertTrue(info.updates[0].contains("on "))
    }
}
