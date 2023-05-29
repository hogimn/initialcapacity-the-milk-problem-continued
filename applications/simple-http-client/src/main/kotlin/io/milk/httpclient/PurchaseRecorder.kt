package io.milk.httpclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.milk.workflow.Worker
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory

/**
 * The PurchaseRecorder class represents a worker responsible for executing purchase tasks by sending an HTTP POST
 * request to a specified URL. It implements the Worker interface with a generic type of PurchaseTask.
 *
 * @property httpClient The OkHttpClient used to send HTTP requests.
 * @property urlString The URL to which the HTTP POST request is sent.
 * @property name The name of the PurchaseRecorder worker (default: "sales-worker").
 */
class PurchaseRecorder(
        private val httpClient: OkHttpClient,
        private val urlString: String,
        override val name: String = "sales-worker"
) : Worker<PurchaseTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()
    private val media = "application/json; charset=utf-8".toMediaType()

    /**
     * Executes the purchase task by sending an HTTP POST request to the specified URL.
     *
     * @param task The purchase task to execute.
     */
    override fun execute(task: PurchaseTask) {
        try {
            // Convert the PurchaseTask to JSON
            val json = mapper.writeValueAsString(task)

            // Create a request body with the JSON data
            val body = json.toRequestBody(media)
            // Build an HTTP POST request with the URL and request body
            val ok = okhttp3.Request.Builder().url(urlString).post(body).build()

            // Log the decrementing operation
            logger.info("decrementing the {} quantity by {} for product_id={}", task.name, task.amount, task.id)

            // Send the HTTP request and close the response
            httpClient.newCall(ok).execute().close()
        } catch (e: Exception) {
            // Log the failure to decrement the quantity
            logger.error(
                    "shoot, failed to decrement the {} quantity by {} for product_id={}",
                    task.name,
                    task.amount,
                    task.id
            )
            // Print the stack trace of the exception
            e.printStackTrace()
        }
    }
}
