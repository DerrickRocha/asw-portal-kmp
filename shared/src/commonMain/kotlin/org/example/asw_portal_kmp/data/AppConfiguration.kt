package org.example.asw_portal_kmp.data

import io.ktor.utils.io.ioDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AppConfiguration(
    private val keyValuePairManager: KeyValuePairManager,
    private val ioDispatcher: CoroutineDispatcher = ioDispatcher()
) {

    suspend fun saveTenantId(tenantId: Int) = withContext(ioDispatcher) {
        keyValuePairManager.saveTenantId(tenantId)
    }
}