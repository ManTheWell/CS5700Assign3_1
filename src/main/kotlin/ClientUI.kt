import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

private val trackedShipments = mutableStateMapOf<String, SerializedShipment>()

@Composable
@Preview
fun clientApp() {
    LaunchedEffect(Unit) {
        listenForShipmentUpdates()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.DarkGray)
            .padding(16.dp)
    ) {
        createSearchBar()

        Spacer(Modifier.height(16.dp))

        trackedShipments.forEach { (shipmentID, shipment) ->
            createBox(shipment)

            Button(onClick = {
                trackedShipments.remove(shipmentID, shipment)
            }) {
                Text("Stop Tracking ${shipment.id}")
            }
        }
    }
}

private fun listenForShipmentUpdates() {
    CoroutineScope(Dispatchers.IO).launch {
        val url = URL("http://localhost:8080/subscribe")
        val conn = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "text/event-stream")

        try {
            BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // strip convention start of "data: " and the two new line end convention
                    if (line!!.startsWith("data: "))
                        line = line!!.replace("data: ", "").replace("\n\n", "")

                    // if the updated shipment is being tracked, update it (auto-refreshes UI)
                    if (trackedShipments.containsKey(line)) {
                        val updated = fetchShipmentById(line!!)
                        if (updated != null) {
                            trackedShipments[line!!] = updated
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private suspend fun fetchShipmentById(id: String): SerializedShipment? {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    return try {
        client.get("http://localhost:8080/shipment/$id").body()
    } catch (e: Exception) {
        println("Error: ${e.message}")
        null
    }
}

@Composable
private fun createSearchBar() {
    val trackingID = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Row {
        TextField(
            value = trackingID.value,
            onValueChange = { trackingID.value = it },
            placeholder = { Text("Enter Tracking ID") },
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(color = Color.White)
        )

        Button(onClick = {
            scope.launch {
                val shipment = fetchShipmentById(trackingID.value)
                if (shipment != null) {
                    trackedShipments[shipment.id] = shipment
                    trackingID.value = ""
                }
            }
        }) {
            Text("Track")
        }
    }
}

@Composable
private fun createBox(shipment: SerializedShipment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White)
            .padding(8.dp)
    ) {
        Text("Shipment ID: ${shipment.id.uppercase(Locale.getDefault())}")
        Text("Status: ${shipment.status}")
        Text("Expected Delivery: ${shipment.expectedDelivery}")

        Spacer(Modifier.height(4.dp))

        Text("Updates:")
        shipment.updates.forEach { update ->
            Text("- $update")
        }

        Spacer(Modifier.height(4.dp))

        Text("Notes:")
        shipment.notes.forEach { note ->
            Text("* $note")
        }

        Spacer(Modifier.height(4.dp))
    }
}