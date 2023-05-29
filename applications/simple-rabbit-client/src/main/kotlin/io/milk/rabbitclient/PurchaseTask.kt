package io.milk.rabbitclient

/**
 * The PurchaseTask data class represents a purchase task with its associated properties.
 *
 * @param id The ID of the purchase task.
 * @param name The name of the purchase task.
 * @param amount The amount/quantity of the purchase task.
 */
data class PurchaseTask(val id: Long, val name: String, val amount: Int)
