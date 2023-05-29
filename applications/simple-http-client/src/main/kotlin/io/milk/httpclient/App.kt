package io.milk.httpclient

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.milk.workflow.WorkScheduler
import okhttp3.OkHttpClient
import java.util.*

/**
 * The main entry point of the Ktor application.
 */
fun Application.module() {
    // Install the routing feature
    install(Routing) {
        get("/") {
            // Respond with a plain text "ok!"
            call.respondText { "ok!" }
        }
    }

    // Get the PRODUCTS_SERVER environment variable or use a default value if not present
    val urlString: String = System.getenv("PRODUCTS_SERVER") ?: "http://localhost:8081/api/v1/products"
    // Create an OkHttpClient for making HTTP requests
    val httpClient = OkHttpClient().newBuilder().build()
    // Create a list of PurchaseRecorder workers
    val workers = (1..4).map {
        PurchaseRecorder(httpClient, urlString)
    }
    // Create a WorkScheduler with a PurchaseGenerator and the list of workers, and set the interval to 10 seconds
    val scheduler = WorkScheduler(PurchaseGenerator(), workers, 10)
    // Start the scheduler
    scheduler.start()
}

/**
 * The main function that starts the Ktor server.
 */
fun main() {
    // Set the default time zone to UTC
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    // Get the port number from the PORT environment variable or use a default value if not present
    val port = System.getenv("PORT")?.toInt() ?: 8082
    // Start the embedded server using the Jetty engine and the module defined above
    embeddedServer(Jetty, port, module = Application::module).start()
}
