import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

open class Shipment (data: List<String>) {
    private val id: String
    private val type: String
    private var status = "Created"
    private var location: String
    private var expDeliveryTime: String

    private val updates = mutableListOf<String>()
    private val notes = mutableListOf<String>()

    init {
        // update time, id, "created", type, expected date

        id = data[1]
        type = data[3]
        location = "Origin Warehouse"
        expDeliveryTime = convertTime(data[4])

        val readableTimestamp = convertTime(data[0])
        updates.add(0, "Created on $readableTimestamp")
    }

    fun getID(): String {
        return id
    }

    fun getData(): SerializedShipment {
        return SerializedShipment(id, type, status, location, expDeliveryTime, updates, notes)
    }

    private fun updateExpectedDelivery(data: List<String>) {
        expDeliveryTime = convertTime(data.getOrNull(3) ?: "")
    }

    fun shipped(data: List<String>): Boolean {
        status = "Shipped"

        updateExpectedDelivery(data)

        updates.add(0, "Shipped on ${convertTime(data[0])}. Expected delivery on $expDeliveryTime")

        return true
    }

    fun location(data: List<String>): Boolean {
        status = "Arrived at New Location"

        location = data.getOrNull(3) ?: ""

        updates.add(0, "New shipment location: $location on ${convertTime(data[0])}")

        return true
    }

    fun delivered(data: List<String>): Boolean {
        status = "Delivered"
        location = "Delivered"
        expDeliveryTime = "Delivery Complete"

        updates.add(0, "Delivered at ${convertTime(data[0])}")

        return true
    }

    fun delayed(data: List<String>): Boolean {
        status = "Delayed"
        updateExpectedDelivery(data)

        updates.add(0, "Delayed on ${convertTime(data[0])}, new expected delivery date $expDeliveryTime")

        return true
    }

    fun lost(data: List<String>): Boolean {
        status = "Lost"
        location = "Unknown"
        expDeliveryTime = "N/A"

        updates.add(0, "Lost on ${convertTime(data[0])}, last known location: $location")

        return true
    }

    fun canceled(data: List<String>): Boolean {
        status = "Canceled"
        location = "N/A"
        expDeliveryTime = "N/A"

        updates.add(0, "Canceled on ${convertTime(data[0])}")

        return true
    }

    fun note(data: List<String>): Boolean {
        note(data[3])

        return true
    }

    protected fun note(note: String) {
        notes.add(0, note)
    }

    private fun convertTime(epocString : String): String {
        val epochMillis: Long = epocString.toLongOrNull() ?: return ""

        val instant = Instant.ofEpochMilli(epochMillis)
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' HH:mm").withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }
}