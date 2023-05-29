package io.milk.start

import com.rabbitmq.client.CancelCallback

/**
 * Handler for cancelling a product update.
 */
class ProductUpdateCancelHandler : CancelCallback {
    /**
     * Handles the cancellation of a product update.
     *
     * @param consumerTag the consumer tag associated with the update
     */
    override fun handle(consumerTag: String) {
    }
}
