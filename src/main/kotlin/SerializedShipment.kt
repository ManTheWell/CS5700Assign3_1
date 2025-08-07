import kotlinx.serialization.Serializable

@Serializable
data class SerializedShipment(
    var id: String,
    val type: String,
    var status: String,
    var location: String,
    var expectedDelivery: String,
    val updates: List<String>,
    val notes: List<String>
)