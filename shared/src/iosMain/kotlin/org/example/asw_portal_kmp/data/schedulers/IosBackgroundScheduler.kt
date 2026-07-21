package org.example.asw_portal_kmp.data.schedulers

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.Foundation.dateByAddingTimeInterval

class IosBackgroundScheduler: BackgroundScheduler {
    override fun schedulePeriodicTask(
        taskId: String,
        workerName: String,
        intervalMs: Long,
        constraints: BackgroundConstraints
    ) {
        // iOS uses BGAppRefreshTask for periodic tasks (short, ~30 seconds)
        val request = BGAppRefreshTaskRequest(taskId).apply {
            earliestBeginDate = NSDate().dateByAddingTimeInterval(intervalMs.toDouble() / 1000.0)
        }

        submitTask(taskId, request, workerName)
    }

    override fun scheduleOneTimeTask(
        taskId: String,
        workerName: String,
        delayMs: Long,
        constraints: BackgroundConstraints
    ) {
        // Determine if it's a processing task (longer) or refresh task
        val request = if (constraints.requiresCharging || constraints.requiresDeviceIdle) {
            // Use BGProcessingTask for longer operations that should happen when device is idle/charging
            BGProcessingTaskRequest(taskId).apply {
                earliestBeginDate = NSDate().dateByAddingTimeInterval(delayMs.toDouble() / 1000.0)
                requiresExternalPower = constraints.requiresCharging
                requiresNetworkConnectivity = constraints.requiresNetwork
            }
        } else {
            // Use BGAppRefreshTask for quick operations
            BGAppRefreshTaskRequest(taskId).apply {
                earliestBeginDate = NSDate().dateByAddingTimeInterval(delayMs.toDouble() / 1000.0)
            }
        }

        submitTask(taskId, request, workerName)
    }

    override fun cancelTask(taskId: String) {
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(taskId)
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun submitTask(taskId: String, request: BGTaskRequest, workerName: String) {
        val error = memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, errorPtr.ptr)
            errorPtr.value
        }

        if (error != null) {
            NSLog("Failed to schedule iOS task: ${error?.localizedDescription}")
        } else {
            // Store the worker name for when the task is executed
            // This is set up in the AppDelegate registration
            IosTaskManager.pendingTasks[taskId] = workerName
        }
    }
}