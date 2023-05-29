package io.milk.rabbitmq

import com.rabbitmq.client.ConnectionFactory

/**
 * Utility class for testing RabbitMQ functionality.
 *
 * @property connectionFactory The RabbitMQ connection factory.
 */
class RabbitTestSupport {
    private val connectionFactory = ConnectionFactory().apply { useBlockingIo() }

    /**
     * Waits for all consumers to finish processing messages in the specified queue.
     *
     * @param queue The name of the queue.
     */
    fun waitForConsumers(queue: String) {
        var count: Long
        do {
            count = messageCount(queue)
            // Wait for 500 milliseconds
            Thread.sleep(500)
        } while (count > 0)
    }

    /**
     * Purges all messages from the specified queue.
     *
     * @param queue The name of the queue.
     */
    fun purge(queue: String) {
        connectionFactory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                // Purge the queue
                channel.queuePurge(queue)
            }
        }
    }

    ///

    /**
     * Retrieves the number of messages in the specified queue.
     *
     * @param queue The name of the queue.
     * @return The number of messages in the queue.
     */
    private fun messageCount(queue: String): Long {
        connectionFactory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                // Get message count in the queue
                return channel.messageCount(queue)
            }
        }
    }
}