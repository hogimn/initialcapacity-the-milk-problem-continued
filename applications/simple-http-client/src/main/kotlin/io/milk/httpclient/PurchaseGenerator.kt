package io.milk.httpclient

import io.milk.workflow.WorkFinder
import org.slf4j.LoggerFactory

/**
 * A PurchaseGenerator that implements the WorkFinder interface for generating purchase tasks.
 */
class PurchaseGenerator : WorkFinder<PurchaseTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Generates a list of PurchaseTasks based on the requested name.
     *
     * @param name The requested name.
     * @return A list of PurchaseTasks.
     */
    override fun findRequested(name: String): List<PurchaseTask> {
        // Generate a random quantity between 1 and 4
        val random = (1..4).random()

        // Log the purchase event
        logger.info("someone purchased some milk!")

        // Create a single PurchaseTask with a hardcoded ID, name, and the generated quantity
        return mutableListOf(PurchaseTask(105442, "milk", random))
    }

    /**
     * Marks the specified PurchaseTask as completed.
     *
     * @param info The completed PurchaseTask.
     */
    override fun markCompleted(info: PurchaseTask) {
        // Log the completion of the purchase
        logger.info("completed purchase")
    }
}
