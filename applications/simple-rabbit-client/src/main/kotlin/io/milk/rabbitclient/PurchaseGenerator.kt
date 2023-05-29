package io.milk.rabbitclient

import io.milk.workflow.WorkFinder
import org.slf4j.LoggerFactory

/**
 * The PurchaseGenerator class implements the WorkFinder interface and is responsible for generating purchase tasks.
 */
class PurchaseGenerator : WorkFinder<PurchaseTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Generates a list of purchase tasks requested for the given [name].
     *
     * @param name The name associated with the requested purchase tasks.
     * @return A list of PurchaseTask objects representing the requested purchase tasks.
     */
    override fun findRequested(name: String): List<PurchaseTask> {
        val random = (1..4).random()

        logger.info("someone purchased some milk!")

        return mutableListOf(PurchaseTask(105442, "milk", random))
    }

    /**
     * Marks the given [info] purchase task as completed.
     *
     * @param info The PurchaseTask object to be marked as completed.
     */
    override fun markCompleted(info: PurchaseTask) {
        logger.info("completed purchase")
    }
}
