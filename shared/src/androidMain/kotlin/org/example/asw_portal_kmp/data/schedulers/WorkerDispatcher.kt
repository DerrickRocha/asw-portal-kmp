package org.example.asw_portal_kmp.data.schedulers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.example.asw_portal_kmp.Dependencies

class WorkerDispatcher(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val workerName = inputData.getString("worker_name") ?: return Result.failure()
        val inputData = inputData.getString("input_params")

        val worker = Dependencies.workerRegistry.getWorker(workerName)
            ?: return Result.failure()

        return try {
            val result = worker.doWork(inputData)
            when (result) {
                is BackgroundResult.Success -> Result.success()
                is BackgroundResult.Failure -> Result.failure()
                BackgroundResult.Retry -> Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}