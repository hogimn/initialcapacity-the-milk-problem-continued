package io.milk.testsupport

import io.milk.database.createDatasource
import javax.sql.DataSource

/**
 * JDBC URL for the test database.
 */
const val testJdbcUrl = "jdbc:postgresql://localhost:5432/milk_test"

/**
 * Username for the test database.
 */
const val testDbUsername = "milk"

/**
 * Password for the test database.
 */
const val testDbPassword = "milk"

/**
 * Creates a DataSource for the test database.
 *
 * @return The created DataSource.
 */
fun testDataSource(): DataSource {
    return createDatasource(
        jdbcUrl = testJdbcUrl,
        username = testDbUsername,
        password = testDbPassword
    )
}
