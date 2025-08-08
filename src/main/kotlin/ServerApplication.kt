import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.call
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.respondTextWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext

private val repositoryManager = RepositoryManager()
private val clients = mutableSetOf<Channel<String>>()

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Post)
    }

    routing {
        get("/subscribe") {
           subscribe(call)
        }

        post("/update") {
            posted(call)
        }

        get("/shipment/{id}") {
            getById(call)
        }
    }
}

private suspend fun subscribe(call: ApplicationCall) {
    val channel = Channel<String>()
    clients.add(channel)

    call.response.cacheControl(CacheControl.NoCache(null))
    call.respondTextWriter(ContentType.Text.EventStream) {
        try {
            for (shipmentId in channel) {
                withContext(Dispatchers.IO) {
                    // SSE protocol format: data: <message>\n\n
                    write("data: $shipmentId\n\n")
                    flush()
                }
            }
        } catch (e: Exception) {
            println("Connection closed: ${e.message}")
        } finally {
            clients.remove(channel)
            channel.close()
        }
    }
}

private suspend fun posted(call: ApplicationCall) {
    val rawInput = call.receive<String>()

    try {
        val updated = repositoryManager.processInput(rawInput)

        // if the update isn't able to be processed, notify client
        if (updated == null || !updated ) {
            call.respond(HttpStatusCode.NotAcceptable, "Unable to process $rawInput")
            return
        }

        call.respond(HttpStatusCode.Accepted, "Update processed: $rawInput")

        // when a shipment is updated, broadcast the shipment ID to clients
        clients.forEach { client ->
            try {
                client.send(rawInput.split(",")[1])
            } catch (e: Exception) {
                println("Failed to send to a client: ${e.message}")
            }
        }
    } catch (e: Exception) {
        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid data")
    }
}

private suspend fun getById(call: ApplicationCall) {
    val id = call.parameters["id"]
    val shipment = repositoryManager.getShipment(id!!)
    if (shipment != null) {
        call.respond(shipment.getData())
    } else {
        call.respond(HttpStatusCode.NotFound, "Shipment not found")
    }
}
