package io.milk.start

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.milk.database.createDatasource
import io.milk.products.ProductDataGateway
import io.milk.products.ProductService
import io.milk.products.PurchaseInfo
import io.milk.rabbitmq.BasicRabbitConfiguration
import io.milk.rabbitmq.BasicRabbitListener
import org.slf4j.LoggerFactory
import java.util.*

/**
 * The main module that configures the server.
 *
 * @param jdbcUrl The JDBC database URL.
 * @param username The database username.
 * @param password The database password.
 */
fun Application.module(jdbcUrl: String, username: String, password: String) {
    val logger = LoggerFactory.getLogger(this.javaClass)
    // Create a data source using the JDBC URL, username, and password
    val dataSource = createDatasource(jdbcUrl, username, password)

    // Create an instance of the ProductService with a ProductDataGateway
    val productService = ProductService(ProductDataGateway(dataSource))

    // Install the DefaultHeaders feature, which adds default HTTP headers to the response.
    install(DefaultHeaders)
    // Install the CallLogging feature, which logs information about each HTTP call made to the server.
    install(CallLogging)
    // Install the FreeMarker feature, which integrates the FreeMarker template engine into the server application.
    install(FreeMarker) {
        // Set the template loader to use the ClassTemplateLoader with the specified class loader and template directory.
        // This allows FreeMarker to load templates from the "templates" directory using the provided class loader.
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(ContentNegotiation) {
        // Use Jackson as the content negotiation converter.
        jackson()
    }
    install(Routing) {
        // Handle GET request to the root path
        get("/") {
            // Retrieve all products from the ProductService
            val products = productService.findAll()
            // Respond with the "index.ftl" FreeMarker template, passing the "products" data as a parameter
            call.respond(FreeMarkerContent("index.ftl", mapOf("products" to products)))
        }
        // Handle POST request to "/api/v1/products"
        post("/api/v1/products") {
            // Receive the request body as a PurchaseInfo object
            val purchase = call.receive<PurchaseInfo>()

            // Retrieve the current inventory information for the specified product ID
            val currentInventory = productService.findBy(purchase.id)
            logger.info(
                "current inventory {}, quantity={}, product_id={}",
                currentInventory.name,
                currentInventory.quantity,
                currentInventory.id
            )

            logger.info(
                "received purchase for {}, quantity={}, product_id={}",
                purchase.name,
                purchase.amount,
                purchase.id
            )

            // Decrement the inventory quantity by the purchased amount
            productService.decrementBy(purchase) // TODO - DIRTY READS - Replace with decrementBy. Why is using update problematic?

            // Respond with HTTP status code 201 (Created) to indicate successful creation of the purchase
            call.respond(HttpStatusCode.Created)
        }
        // Serve static resources under the "images" directory
        static("images") { resources("images") }
        // Serve static resources under the "style" directory
        static("style") { resources("style") }
    }

    // Set up the RabbitMQ configuration for the "products" queue,
    // which includes specifying the exchange, queue, and routing key.
    BasicRabbitConfiguration(exchange = "products-exchange", queue = "products", routingKey = "auto").setUp()
    // Start the RabbitMQ listener for the "products" queue.
    // - The `queue` parameter specifies the queue to listen to.
    // - The `delivery` parameter specifies the handler for processing the received messages.
    // - The `cancel` parameter specifies the handler for handling listener cancellations.
    // - The `autoAck` parameter specifies whether to automatically acknowledge the received messages.
    BasicRabbitListener(
        queue = "products",
        delivery = ProductUpdateHandler(productService),
        cancel = ProductUpdateCancelHandler(),
        autoAck = true,
    ).start()

    // TODO - MESSAGING -
    //  set up the rabbit configuration for your safer queue and
    //  start the rabbit listener with the safer product update handler **with manual acknowledgement**
    //  this looks similar to the above invocation
    // Set up the RabbitMQ configuration for the "safer-products" queue,
    // which includes specifying the exchange, queue, and routing key.
    BasicRabbitConfiguration(
            exchange = "products-exchange",
            queue = "safer-products",
            routingKey = "safer"
    ).setUp()
    // Start the RabbitMQ listener for the "safer-products" queue.
    // In this case, `autoAck` is set to `false`, indicating manual acknowledgement.
    BasicRabbitListener(
            queue = "safer-products",
            delivery = SaferProductUpdateHandler(productService),
            cancel = ProductUpdateCancelHandler(),
            autoAck = false
    ).start()
}

/**
 * The main function that starts the server.
 */
fun main() {
    // Set the default timezone to UTC
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    // Get the port from the environment variable or use a default value of 8081
    val port = System.getenv("PORT")?.toInt() ?: 8081
    // Get the JDBC database URL from the environment variable
    val jdbcUrl = System.getenv("JDBC_DATABASE_URL")
    // Get the database username from the environment variable
    val username = System.getenv("JDBC_DATABASE_USERNAME")
    // Get the database password from the environment variable
    val password = System.getenv("JDBC_DATABASE_USERNAME")

    // Start the embedded server using the Jetty engine, specifying the port and the module
    embeddedServer(Jetty, port, module = { module(jdbcUrl, username, password) }).start()
}