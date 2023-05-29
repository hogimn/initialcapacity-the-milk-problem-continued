package io.milk.start

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import io.milk.products.ProductService
import io.milk.products.PurchaseInfo
import io.milk.rabbitmq.ChannelDeliverCallback
import org.slf4j.LoggerFactory

/**
 * Handles incoming messages from RabbitMQ for product updates.
 * Implements the ChannelDeliverCallback interface.
 *
 * @param service The ProductService instance used to perform business logic related to the received messages.
 */
class ProductUpdateHandler(private val service: ProductService) : ChannelDeliverCallback {
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
        // Perform the necessary business logic using the ProductService to handle the received purchase.
        // In this case, it calls the decrementBy() function of the ProductService.
        service.decrementBy(purchase) // TODO - MESSAGING - replace with decrementBy
    }
}