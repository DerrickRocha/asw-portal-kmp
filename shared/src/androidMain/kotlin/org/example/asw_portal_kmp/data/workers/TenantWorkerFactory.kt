package org.example.asw_portal_kmp.data.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import org.example.asw_portal_kmp.data.repositories.tenants.TenantsRepository

class TenantWorkerFactory(
    private val repository: TenantsRepository
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName) {
            TenantsWorker::class.java.name -> TenantsWorker(appContext, workerParameters, repository)
            else -> null
        }
    }
}