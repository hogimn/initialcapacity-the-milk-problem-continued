package io.milk.products

/**
 * Represents information about a purchase.
 *
 * @property id The product ID.
 * @property name The name of the product.
 * @property amount The purchase amount.
 */
data class PurchaseInfo(val id: Long, val name: String, val amount: Int)