package io.milk.start

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Delivery
import io.ktor.utils.io.*
import io.milk.products.ProductService
import io.milk.products.PurchaseInfo
import io.milk.rabbitmq.ChannelDeliverCallback
import org.slf4j.LoggerFactory

/**
 * Handles incoming messages from RabbitMQ for safer product updates.
 * Implements the ChannelDeliverCallback interface.
 *
 * @param service The ProductService instance used to perform business logic related to the received messages.
 */
class SaferProductUpdateHandler(private val service: ProductService) : ChannelDeliverCallback {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val mapper = ObjectMapper().registerKotlinModule()
    private var channel: Channel? = null

    /**
     * Sets the Channel instance associated with the message delivery.
     *
     * @param channel The Channel instance associated with the message delivery.
     */
    override fun setChannel(channel: Channel) {
        this.channel = channel
    }

    /**
     * Handles the incoming message from RabbitMQ.
     *
     * @param consumerTag The consumer tag associated with the listener.
     * @param message The Delivery object representing the received message.
     */
    override fun handle(consumerTag: String, message: Delivery) {
        // Read the message body and deserialize it as a PurchaseInfo object using the ObjectMapper.
        val purchase = mapper.readValue<PurchaseInfo>(message.body)

        logger.info(
            "received event. purchase for {}, quantity={}, product_id={}",
            purchase.name,
            purchase.amount,
            purchase.id
        )

        try {
            service.decrementBy(purchase)

            // TODO - MESSAGING - can we prevent a failure here?
            //  randomly throw an exception for bacon
            //  ensure the testBestCase test passes
            randomlyThrowAnExceptionForBacon(purchase.name)

            // Acknowledge the message delivery by sending a positive acknowledgement to RabbitMQ.
            channel!!.basicAck(message.envelope.deliveryTag, true)

        } catch (e: Exception) {
            // If an exception occurs during processing, print the stack trace and reject the message.
            e.printStack()
            channel!!.basicReject(message.envelope.deliveryTag, true)
        }
    }

    /**
     * Randomly throws an exception for bacon purchase.
     *
     * @param name The name of the purchase.
     */
    private fun randomlyThrowAnExceptionForBacon(name: String) {
        if ((1..2).random() == 1 && name == "bacon") {
            throw Exception("shoot, something bad happened.")
        }
    }
}