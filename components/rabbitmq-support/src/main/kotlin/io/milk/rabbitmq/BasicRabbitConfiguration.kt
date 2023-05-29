package io.milk.rabbitmq

import com.rabbitmq.client.ConnectionFactory

/**
 * Configuration class for basic RabbitMQ setup.
 *
 * @property exchange The name of the exchange.
 * @property queue The name of the queue.
 * @property routingKey The routing key.
 */
class BasicRabbitConfiguration(private val exchange: String, private val queue: String, private val routingKey: String) {

    /**
     * Sets up the RabbitMQ configuration.
     * Creates a connection factory, establishes a connection, and declares the exchange, queue, and binding.
     */
    fun setUp() {
        // Create a new ConnectionFactory
        val connectionFactory = ConnectionFactory().apply { useBlockingIo() }
        // Establish a connection to RabbitMQ
        val connection = connectionFactory.newConnection()

        // Create a channel within the connection
        connection.createChannel().use { channel ->
            // Declare the exchange with the given name, type, durable, autoDelete, and additional arguments
            channel.exchangeDeclare(exchange, "direct", false, false, null)
            // Declare the queue with the given name, durable, exclusive, autoDelete, and additional arguments
            channel.queueDeclare(queue, false, false, false, null)
            // Bind the queue to the exchange with the given routing key
            channel.queueBind(queue, exchange, routingKey)
        }
    }
}