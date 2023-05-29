package io.milk.httpclient

/**
 * The PurchaseTask data class represents a purchase task.
 *
 * @property id The unique identifier for the purchase task.
 * @property name The name of the product to be purchased.
 * @property amount The quantity or amount of the product to be purchased.
 */
data class PurchaseTask(val id: Long, val name: String, val amount: Int)
