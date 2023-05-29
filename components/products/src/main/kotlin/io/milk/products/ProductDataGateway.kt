package io.milk.products

import io.milk.database.DatabaseTemplate
import io.milk.database.TransactionManager
import org.slf4j.LoggerFactory
import javax.sql.DataSource

/**
 * ProductDataGateway is responsible for interacting with the products table in the database.
 *
 * @param dataSource The data source used to obtain database connections.
 */
class ProductDataGateway(private val dataSource: DataSource) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val template = DatabaseTemplate(dataSource)

    /**
     * Creates a new product with the given name and quantity in the database.
     *
     * @param name The name of the product.
     * @param quantity The quantity of the product.
     * @return The created ProductRecord object.
     */
    fun create(name: String, quantity: Int): ProductRecord {
        return template.create(
            "insert into products (name, quantity) values (?, ?)", { id ->
                ProductRecord(id, name, quantity)
            }, name, quantity
        )
    }

    /**
     * Retrieves all product records from the database.
     *
     * @return The list of all product records.
     */
    fun findAll(): List<ProductRecord> {
        return template.findAll("select id, name, quantity from products order by id") { rs ->
            ProductRecord(rs.getLong(1), rs.getString(2), rs.getInt(3))
        }
    }

    /**
     * Retrieves a product record from the database by its ID.
     *
     * @param id The ID of the product to retrieve.
     * @return The product record with the specified ID, or null if not found.
     */
    fun findBy(id: Long): ProductRecord? {
        return template.findBy(
            "select id, name, quantity from products where id = ?", { rs ->
                ProductRecord(rs.getLong(1), rs.getString(2), rs.getInt(3))
            }, id
        )
    }

    /**
     * Updates the specified product record in the database.
     *
     * @param product The product record to update.
     * @return The updated product record.
     */
    fun update(product: ProductRecord): ProductRecord {
        template.update(
            "update products set name = ?, quantity = ? where id = ?",
            product.name, product.quantity, product.id
        )
        return product
    }

    /**
     * Decrements the quantity of a product by the specified purchase amount within a transaction.
     *
     * @param purchase The purchase information containing the product ID and purchase amount.
     */
    fun decrementBy(purchase: PurchaseInfo) {
        return TransactionManager(dataSource).withTransaction {
            val found = template.findBy(
                it,
                "select id, name, quantity from products where id = ? for update", { rs ->
                    ProductRecord(rs.getLong(1), rs.getString(2), rs.getInt(3))
                }, purchase.id
            )
            template.update(
                it,
                "update products set quantity = ? where id = ?",
                (found!!.quantity - purchase.amount), purchase.id
            )
        }
    }

    /**
     * Faster version of decrementing the quantity of a product by the specified purchase amount.
     * This function directly updates the quantity in the database without retrieving the product record.
     *
     * @param purchase The purchase information containing the product ID, name, and purchase amount.
     */
    fun fasterDecrementBy(purchase: PurchaseInfo) {
        logger.info(
            "decrementing the {} quantity by {} for product_id={}",
            purchase.name,
            purchase.amount,
            purchase.id
        )

        return TransactionManager(dataSource).withTransaction {
            template.update(
                it,
                "update products set quantity = (quantity - ?) where id = ?",
                purchase.amount, purchase.id
            )
        }
    }
}