package io.milk.products

/**
 * Service class for managing product-related operations.
 *
 * @property dataGateway The data gateway for accessing product data.
 */
class ProductService(private val dataGateway: ProductDataGateway) {
    /**
     * Retrieves all products.
     *
     * @return The list of all products.
     */
    fun findAll(): List<ProductInfo> {
        return dataGateway.findAll().map { ProductInfo(it.id, it.name, it.quantity) }
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id The ID of the product.
     * @return The product with the specified ID.
     */
    fun findBy(id: Long): ProductInfo {
        val record = dataGateway.findBy(id)!!
        return ProductInfo(record.id, record.name, record.quantity)
    }

    /**
     * Updates the quantity of a product after a purchase.
     *
     * @param purchase The purchase information.
     * @return The updated product information.
     */
    fun update(purchase: PurchaseInfo): ProductInfo {
        val record = dataGateway.findBy(purchase.id)!!
        record.quantity -= purchase.amount
        dataGateway.update(record)
        return findBy(record.id)
    }

    /**
     * Decrements the quantity of a product by the specified amount.
     *
     * @param purchase The purchase information.
     */
    fun decrementBy(purchase: PurchaseInfo) {
        // TODO - DIRTY READS - Implement the decrementBy function.
        return dataGateway.decrementBy(purchase)
    }
}