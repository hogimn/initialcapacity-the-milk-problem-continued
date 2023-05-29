package io.milk.rabbitmq

import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback

/**
 * Custom interface that extends the RabbitMQ `DeliverCallback` interface and adds a method to set the channel.
 */
interface ChannelDeliverCallback: DeliverCallback {
    /**
     * Sets the channel for message delivery.
     *
     * @param channel The channel to set.
     */
    fun setChannel(channel: Channel)
}