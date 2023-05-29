package io.milk.rabbitclient

import com.fasterxml.jackson.databind.SerializationFeature
import com.rabbitmq.client.ConnectionFactory
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.milk.workflow.WorkScheduler
import java.util.*

/**
 * The main entry point of the Ktor application.
 */
fun Application.module() {
    // Install the ContentNegotiation feature for JSON serialization.
    install(ContentNegotiation) {
        jackson {
            // Enable indentation of JSON output.
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    // Install the Routing feature and define routes.
    install(Routing) {
        get("/") {
            // Handle GET request to the root path ("/").
            // Respond with plain text response "ok!".
            call.respondText { "ok!" }
        }
    }

    // Create a list of PurchaseRecorder workers.
    val workers = (1..4).map {
        PurchaseRecorder(ConnectionFactory().apply { useNio() }, "auto", "event-worker")
    }

    // Create a WorkScheduler with PurchaseGenerator as the task generator,
    // the list of PurchaseRecorder workers, and a delay of 30 milliseconds between tasks.
    val scheduler = WorkScheduler(PurchaseGenerator(), workers, 30)

    // Start the scheduler.
    scheduler.start()
}

/**
 * The main function that starts the Ktor server.
 */
fun main() {
    // Set the default time zone to UTC.
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    // Get the port number from the environment variable or use the default value 8083.
    val port = System.getenv("PORT")?.toInt() ?: 8083

    // Start the embedded Jetty server with the module function as the application module.
    embeddedServer(Jetty, port, module = Application::module).start()
}
