package org.example.asw_portal_kmp.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.example.asw_portal_kmp.data.repositories.tenants.TenantsRepository

class TenantsWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val repository: TenantsRepository,
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            repository.executeNetworkSync()
            return Result.success()
        } catch(_: Exception){
            return Result.failure()
        }
    }
}