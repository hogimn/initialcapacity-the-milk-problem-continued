package io.milk.database

import java.sql.Connection
import javax.sql.DataSource

/**
 * TransactionManager is responsible for managing transactions by providing a higher-order function
 * that executes a given function within a transaction context.
 *
 * @param dataSource The data source used to obtain database connections.
 */
class TransactionManager(private val dataSource: DataSource) {

    /**
     * Executes the given function within a transaction context.
     *
     * @param function The function to be executed within a transaction context.
     * @return The result returned by the function.
     */
    fun <T> withTransaction(function: (Connection) -> T): T {
        dataSource.connection.use { connection ->
            // Disable auto-commit to start a transaction
            connection.autoCommit = false
            // Disable auto-commit to start a transaction
            val results = function(connection)
            // Commit the transaction
            connection.commit()
            // Enable auto-commit after the transaction is completed
            connection.autoCommit = true
            // Return the result from the executed function
            return results
        }
    }
}