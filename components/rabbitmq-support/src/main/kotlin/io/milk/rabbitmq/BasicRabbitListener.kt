package io.milk.rabbitmq

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * RabbitMQ listener for consuming messages from a queue.
 *
 * @property queue The name of the queue to listen to.
 * @property delivery The callback for handling message delivery.
 * @property cancel The callback for handling cancellation.
 * @property autoAck Specifies if automatic message acknowledgment is enabled (default: true).
 */
class BasicRabbitListener(
    private val queue: String,
    private val delivery: ChannelDeliverCallback,
    private val cancel: CancelCallback,
    private val autoAck: Boolean = true
) {
    private var running = true
    private val connectionFactory = ConnectionFactory().apply { useBlockingIo() }

    /**
     * Starts the listener in a separate thread.
     * Creates a connection, channel, and consumes messages from the queue.
     */
    fun start() {
        thread {
            connectionFactory.newConnection().use { connection ->
                connection.createChannel().use { channel ->
                    // Set the delivery callback for handling message delivery
                    delivery.setChannel(channel)

                    while (running) {
                        try {
                            // Start consuming messages from the queue with the specified callbacks
                            channel.basicConsume(queue, autoAck, delivery, cancel)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        // Wait for a short period before consuming the next message
                        sleep(100)
                    }
                }
            }
        }
    }

    /**
     * Stops the listener.
     * Sets the running flag to false and waits for a short period.
     * Note: The wait period is added for testing purposes.
     */
    fun stop() {
        running = false
        sleep(100) // wait for a cycle, needed for testing
    }
}