package org.example.asw_portal_kmp.data.schedulers.tenants

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.example.asw_portal_kmp.data.repositories.tenants.TenantsRepository
import org.example.asw_portal_kmp.data.schedulers.BackgroundResult
import org.example.asw_portal_kmp.data.schedulers.BackgroundWorker

class TenantsWorker(
    private val repository: TenantsRepository,
    private val dispatcher: CoroutineDispatcher
) : BackgroundWorker {
    override suspend fun doWork(params: String?): BackgroundResult = withContext(dispatcher) {
        try {
            repository.executeNetworkSync()
        } catch (exception: Exception) {
            return@withContext BackgroundResult.Failure(exception.message ?: "Failed to sync tenants")
        }
        return@withContext BackgroundResult.Success(
            "Tenants synced successfully"
        )
    }
}