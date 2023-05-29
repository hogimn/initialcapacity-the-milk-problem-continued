package io.milk.products

/**
 * Data class representing a product record.
 *
 * @property id The ID of the product.
 * @property name The name of the product.
 * @property quantity The quantity of the product.
 */
data class ProductRecord(val id: Long, val name: String, var quantity: Int)