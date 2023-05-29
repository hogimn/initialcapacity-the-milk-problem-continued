package io.milk.database

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.time.LocalDate
import java.util.*
import javax.sql.DataSource

/**
 * The DatabaseTemplate class provides utility methods for interacting with a database using JDBC.
 *
 * @property dataSource The DataSource object representing the database connection.
 */
class DatabaseTemplate(val dataSource: DataSource) {

    /**
     * Executes an SQL INSERT statement with the given parameters and returns the generated ID.
     *
     * @param sql The SQL INSERT statement.
     * @param id The function to extract the generated ID from the result set.
     * @param params The parameters to bind to the SQL statement.
     * @return The generated ID.
     */
    fun <T> create(sql: String, id: (Long) -> T, vararg params: Any) =
        // Acquire a database connection using the data source
        dataSource.connection.use { connection ->
            // Delegate to the overloaded create function with the connection
            create(connection, sql, id, *params)
        }

    /**
     * Executes an SQL INSERT statement with the given connection, parameters, and returns the generated ID.
     *
     * @param connection The database connection.
     * @param sql The SQL INSERT statement.
     * @param id The function to extract the generated ID from the result set.
     * @param params The parameters to bind to the SQL statement.
     * @return The generated ID.
     */
    fun <T> create(connection: Connection, sql: String, id: (Long) -> T, vararg params: Any): T {
        // Prepare the SQL statement with generated keys support
        return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { statement ->
            // Set the parameters on the statement
            setParameters(params, statement)
            // Execute the SQL statement
            statement.executeUpdate()
            // Retrieve the generated keys result set
            val keys = statement.generatedKeys
            // Move to the first row
            keys.next()
            // Extract the generated ID using the provided function
            id(keys.getLong(1))
        }
    }

    /**
     * Executes an SQL SELECT statement and maps the result set to a list of objects using the provided mapper function.
     *
     * @param sql The SQL SELECT statement.
     * @param mapper The function to map the result set to an object.
     * @return The list of mapped objects.
     */
    fun <T> findAll(sql: String, mapper: (ResultSet) -> T): List<T> {
        // Call the query function with empty parameters and the provided mapper
        return query(sql, {}, mapper)
    }

    /**
     * Executes an SQL SELECT statement with the given connection and maps the result set to a list of objects
     * using the provided mapper function.
     *
     * @param connection The database connection.
     * @param sql The SQL SELECT statement.
     * @param mapper The function to map the result set to an object.
     * @return The list of mapped objects.
     */
    fun <T> findAll(connection: Connection, sql: String, mapper: (ResultSet) -> T): List<T> {
        // Call the query function with the provided connection,
        // SQL statement, empty parameters, and the provided mapper
        return query(connection, sql, {}, mapper)
    }

    /**
     * Executes an SQL SELECT statement with the given SQL, mapper function, and ID, and returns the corresponding object
     * or null if no object is found.
     *
     * @param sql The SQL SELECT statement.
     * @param mapper The function to map the result set to an object.
     * @param id The ID used to query the object.
     * @return The corresponding object or null if not found.
     */
    fun <T> findBy(sql: String, mapper: (ResultSet) -> T, id: Long): T? {
        dataSource.connection.use { connection ->
            // Call the overloaded findBy function with the provided connection, SQL statement, mapper, and ID
            return findBy(connection, sql, mapper, id)
        }
    }

    /**
     * Executes an SQL SELECT statement with the given connection, SQL, mapper function, and ID,
     * and returns the corresponding object or null if no object is found.
     *
     * @param connection The database connection.
     * @param sql The SQL SELECT statement.
     * @param mapper The function to map the result set to an object.
     * @param id The ID used to query the object.
     * @return The corresponding object or null if not found.
     */
    fun <T> findBy(connection: Connection, sql: String, mapper: (ResultSet) -> T, id: Long): T? {
        // Execute a query with the provided connection, SQL, parameter setter, and mapper function
        val list = query(connection, sql, { ps -> ps.setLong(1, id) }, mapper)
        when {
            // If the list is empty, return null indicating no object found
            list.isEmpty() -> return null

            // Otherwise, return the first object in the list
            else -> return list.first()
        }
    }

    /**
     * Executes an SQL UPDATE statement with the given SQL and parameters.
     *
     * @param sql The SQL UPDATE statement.
     * @param params The parameters to bind to the SQL statement.
     */
    fun update(sql: String, vararg params: Any) {
        // Obtain a connection from the dataSource
        dataSource.connection.use { connection ->
            // Execute the update function with the obtained connection, SQL statement, and parameters
            update(connection, sql, *params)
        }
    }

    /**
     * Executes an SQL UPDATE statement with the given connection, SQL, and parameters.
     *
     * @param connection The database connection.
     * @param sql The SQL UPDATE statement.
     * @param params The parameters to bind to the SQL statement.
     */
    fun update(connection: Connection, sql: String, vararg params: Any) {
        // Prepare the statement using the provided connection
        return connection.prepareStatement(sql).use { statement ->
            // Set the parameters on the prepared statement
            setParameters(params, statement)
            // Execute the update and obtain the number of affected rows
            statement.executeUpdate()
        }
    }

    /**
     * Executes an SQL SELECT statement with the given SQL, parameter binding function, and result set mapper,
     * and returns a list of mapped objects.
     *
     * @param sql The SQL SELECT statement.
     * @param params The function to set the parameters on the prepared statement.
     * @param mapper The function to map the result set to an object.
     * @return The list of mapped objects.
     */
    fun <T> query(sql: String, params: (PreparedStatement) -> Unit, mapper: (ResultSet) -> T): List<T> {
        // Use the data source's connection and execute the query
        dataSource.connection.use { connection ->
            // Delegate to the overloaded query function with the connection
            return query(connection, sql, params, mapper)
        }
    }

    /**
     * Executes an SQL query with the given connection, SQL statement, parameter binding function, and result set mapper,
     * and returns a list of mapped objects.
     *
     * @param connection The database connection.
     * @param sql The SQL SELECT statement.
     * @param params The function to set the parameters on the prepared statement.
     * @param mapper The function to map each row of the result set to an object.
     * @return The list of mapped objects.
     */
    fun <T> query(
        connection: Connection,
        sql: String,
        params: (PreparedStatement) -> Unit,
        mapper: (ResultSet) -> T
    ): List<T> {
        // Create an empty list to store the mapped objects
        val results = ArrayList<T>()
        // Prepare the statement and execute the query
        connection.prepareStatement(sql).use { statement ->
            // Set the parameters on the prepared statement
            params(statement)
            // Execute the query and process the result set
            statement.executeQuery().use { rs ->
                // Iterate over the result set and map each row to an object
                while (rs.next()) {
                    results.add(mapper(rs))
                }
            }
        }
        // Return the list of mapped objects
        return results
    }

    /**
     * Sets the parameters on the given prepared statement based on the provided parameter values.
     *
     * @param params The array of parameter values.
     * @param statement The prepared statement to set the parameters on.
     */
    private fun setParameters(params: Array<out Any>, statement: PreparedStatement) {
        for (i in params.indices) {
            // Retrieve the current parameter value and index
            val param = params[i]
            val parameterIndex = i + 1

            // Set the parameter value based on its type
            when (param) {
                is String -> statement.setString(parameterIndex, param)
                is Int -> statement.setInt(parameterIndex, param)
                is Long -> statement.setLong(parameterIndex, param)
                is Boolean -> statement.setBoolean(parameterIndex, param)
                is LocalDate -> statement.setDate(parameterIndex, Date.valueOf(param))

            }
        }
    }

    /// USED FOR TESTING

    /**
     * Executes the given SQL statement using a CallableStatement.
     *
     * Note: CallableStatement is used when executing stored procedures or functions that return values.
     *
     * @param sql The SQL statement to execute.
     */
    fun execute(sql: String) {
        dataSource.connection.use { connection ->
            connection.prepareCall(sql).use(CallableStatement::execute)
        }
    }
}