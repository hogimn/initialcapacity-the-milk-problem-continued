package io.milk.rabbitclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.MessageProperties
import io.milk.workflow.Worker
import org.slf4j.LoggerFactory

/**
 * The PurchaseRecorder class is responsible for executing a purchase task by sending a decrement event
 * to the messaging system.
 *
 * @param factory The ConnectionFactory to create connections and channels for messaging.
 * @param routingKey The routing key for the messaging system.
 * @param name The name of the PurchaseRecorder worker (default: "sales-worker").
 */
class PurchaseRecorder(
    private val factory: ConnectionFactory,
    private val routingKey: String,
    override val name: String = "sales-worker"
) : Worker<PurchaseTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Executes the purchase task by sending a decrement event to the messaging system.
     *
     * @param task The purchase task to be executed.
     */
    override fun execute(task: PurchaseTask) {
        // Log the information about the event being sent to decrement the quantity for the product
        logger.info("sending event. decrementing the {} quantity by {} for product_id={}", task.name, task.amount, task.id)

        try {
            // Establish a new connection using the ConnectionFactory
            factory.newConnection().use { connection ->
                // Create a channel within the connection
                connection.createChannel().use { channel ->
                    // Convert the purchase task to JSON
                    val body = mapper.writeValueAsString(task).toByteArray()

                    // Publish the JSON message to the "products-exchange" with the provided routing key
                    channel.basicPublish("products-exchange", routingKey, MessageProperties.PERSISTENT_BASIC, body)
                }
            }
        } catch (e: Exception) {
            // Log an error message if an exception occurs during the execution
            logger.error(
                "shoot, failed to decrement the {} quantity by {} for product_id={}",
                task.name,
                task.amount,
                task.id
            )
            e.printStackTrace()
        }
    }
}
