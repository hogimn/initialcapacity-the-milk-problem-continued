package io.milk.database

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

/**
 * Creates a DataSource object using the HikariCP connection pooling library.
 *
 * @param jdbcUrl The JDBC URL of the database.
 * @param username The username for the database connection.
 * @param password The password for the database connection.
 * @return The DataSource object for the configured database connection.
 */
fun createDatasource(
    jdbcUrl: String, username: String, password: String,
): DataSource = HikariDataSource().apply {
    // Set the maximum pool size to 2 connections.
    maximumPoolSize = 2
    // Set the JDBC URL for the database connection.
    setJdbcUrl(jdbcUrl)
    // Set the username for the database connection.
    setUsername(username)
    // Set the password for the database connection.
    setPassword(password)
}
