package io.milk.workflow

import org.slf4j.LoggerFactory

/**
 * Implementation of a Worker that performs a no-operation (noop) task.
 *
 * @property name The name of the NoopWorker.
 */
class NoopWorker(override val name: String = "noop-worker") : Worker<NoopTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Executes the noop task.
     *
     * @param task The NoopTask to execute.
     */
    override fun execute(task: NoopTask) {
        logger.info("doing work. {} {}", task.name, task.value)
    }
}