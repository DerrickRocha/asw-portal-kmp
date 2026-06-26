package org.example.asw_portal_kmp.network.api.tenants

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.example.asw_portal_kmp.network.NetworkManager
import org.example.asw_portal_kmp.network.NetworkResult
import org.example.asw_portal_kmp.network.RequestOptions
import org.example.asw_portal_kmp.network.getJson

interface TenantsRepository {

    suspend fun getTenants(): Result<List<Tenant>>
    suspend fun createTenant(name: String, domain: String, customDomain: String?): Result<Tenant>
}

class TenantsRepositoryImplementation(
    private val networkManager: NetworkManager,
    private val dispatcher: CoroutineDispatcher
) : TenantsRepository {

    override suspend fun getTenants(): Result<List<Tenant>> = withContext(dispatcher) {
        try {
            val response = networkManager.getJson<List<Tenant>>(
                url = "/tenants/all",
                options = RequestOptions(
                    isAuthRequired = true,
                    isTenantRequired = false
                )
            )

            when (response) {
                is NetworkResult.Success -> {
                    Result.Success(response.data)
                }

                is NetworkResult.Error -> {
                    Result.Failure(response.message)
                }

                is NetworkResult.Exception -> {
                    Result.Failure(response.throwable.message ?: "Failed to fetch tenants")
                }
            }
        } catch (e: Exception) {
            Result.Failure(e.message ?: "An unexpected error occurred")
        }
    }

    override suspend fun createTenant(
        name: String,
        domain: String,
        customDomain: String?
    ): Result<Tenant> {
        return Result.Success(Tenant(1, name, domain, customDomain ?: domain, "", "", ""))
    }

}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val message: String) : Result<Nothing>()
}