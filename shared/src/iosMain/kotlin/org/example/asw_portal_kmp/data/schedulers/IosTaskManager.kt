package org.example.asw_portal_kmp.data.schedulers

import io.ktor.utils.io.ioDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.Dependencies
import platform.BackgroundTasks.BGAppRefreshTask

object IosTaskManager {
    val pendingTasks = mutableMapOf<String, String>()
    private val workerRegistry by lazy {
        // This needs to be set from the factory
        Dependencies.workerRegistry
    }
    private val backgroundScope = CoroutineScope(ioDispatcher() + SupervisorJob())

    fun executeBackgroundTask(taskId: String, task: BGAppRefreshTask) {
        // Retrieve the registered worker instance from common dependencies
        val worker = Dependencies.workerRegistry.getWorker(taskId)
        if (worker == null) {
            task.setTaskCompletedWithSuccess(false)
            return
        }

        // Create a specific job to track this run's execution lifeline
        val executionJob = backgroundScope.launch {
            try {
                val result = worker.doWork(params = null)

                when (result) {
                    is BackgroundResult.Success -> {
                        // CRITICAL: Reschedule the next periodic run interval
                        Dependencies.scheduler.schedulePeriodicTask(
                            taskId = taskId,
                            workerName = taskId,
                            intervalMs = 15 * 60 * 1000 // 15 Minutes Baseline
                        )
                        task.setTaskCompletedWithSuccess(true)
                    }
                    is BackgroundResult.Failure -> {
                        task.setTaskCompletedWithSuccess(false)
                    }

                    BackgroundResult.Retry -> TODO()
                }
            } catch (_: Exception) {
                task.setTaskCompletedWithSuccess(false)
            }
        }

        // Handle native iOS system timeout constraints gracefully
        task.expirationHandler = {
            executionJob.cancel("iOS background execution time limit exceeded")
            task.setTaskCompletedWithSuccess(false)
        }
    }
}