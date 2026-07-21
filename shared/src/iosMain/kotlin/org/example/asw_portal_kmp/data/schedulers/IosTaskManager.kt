package org.example.asw_portal_kmp.data.schedulers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.Dependencies
import platform.BackgroundTasks.BGTask

object IosTaskManager {
    val pendingTasks = mutableMapOf<String, String>()
    private val workerRegistry by lazy {
        // This needs to be set from the factory
        Dependencies.workerRegistry
    }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun executeBackgroundTask(taskId: String, task: BGTask) {
        val workerName = pendingTasks.remove(taskId) ?: return

        // Schedule the next task if this is a recurring task
        // The rescheduling logic depends on your app's needs

        scope.launch {
            try {
                val worker = workerRegistry.getWorker(workerName)
                val result = worker?.doWork(null)

                when (result) {
                    is BackgroundResult.Success -> task.setTaskCompletedWithSuccess(success = true)
                    is BackgroundResult.Failure -> task.setTaskCompletedWithSuccess(success = false)
                    BackgroundResult.Retry -> {
                        // For retry, we could schedule a new task
                        // or let the system handle it
                        task.setTaskCompletedWithSuccess(success = false)
                    }
                    null -> task.setTaskCompletedWithSuccess(success = false)
                }
            } catch (e: Exception) {
                task.setTaskCompletedWithSuccess(success = false)
            }
        }
    }
}