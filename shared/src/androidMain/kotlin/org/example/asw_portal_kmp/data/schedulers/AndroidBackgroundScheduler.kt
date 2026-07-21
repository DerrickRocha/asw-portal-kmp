package org.example.asw_portal_kmp.data.schedulers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class AndroidBackgroundScheduler(
    private val context: Context,
) : BackgroundScheduler {

    override fun schedulePeriodicTask(
        taskId: String,
        workerName: String,
        intervalMs: Long,
        constraints: BackgroundConstraints
    ) {
        val workConstraints = buildConstraints(constraints)

        // Pass worker name as input data
        val inputData = workDataOf("worker_name" to workerName)

        val workRequest = PeriodicWorkRequestBuilder<WorkerDispatcher>(
            intervalMs,
            TimeUnit.MILLISECONDS
        )
            .setConstraints(workConstraints)
            .setInputData(inputData)
            .addTag(taskId)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            taskId,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun scheduleOneTimeTask(
        taskId: String,
        workerName: String,
        delayMs: Long,
        constraints: BackgroundConstraints
    ) {
        val workConstraints = buildConstraints(constraints)
        val inputData = workDataOf("worker_name" to workerName)

        val workRequest = OneTimeWorkRequestBuilder<WorkerDispatcher>()
            .setConstraints(workConstraints)
            .setInputData(inputData)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(taskId)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            taskId,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun cancelTask(taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(taskId)
    }

    private fun buildConstraints(constraints: BackgroundConstraints): Constraints {
        return Constraints.Builder().apply {
            if (constraints.requiresNetwork) {
                if (constraints.requiresUnmeteredNetwork) {
                    setRequiredNetworkType(NetworkType.UNMETERED)
                } else {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                }
            }
            if (constraints.requiresCharging) {
                setRequiresCharging(true)
            }
            if (constraints.requiresDeviceIdle) {
                setRequiresDeviceIdle(true)
            }
            if (constraints.requiresStorageNotLow) {
                setRequiresStorageNotLow(true)
            }
        }.build()
    }
}