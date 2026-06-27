package org.example.asw_portal_kmp.network.api.tenants

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.example.asw_portal_kmp.network.NetworkManager
import org.example.asw_portal_kmp.network.NetworkResult
import org.example.asw_portal_kmp.network.api.RepositoryResult
import org.example.asw_portal_kmp.network.RequestOptions
import org.example.asw_portal_kmp.network.getJson
import org.example.asw_portal_kmp.network.postJson

interface TenantsRepository {

    suspend fun getTenants(): RepositoryResult<List<Tenant>>
    suspend fun createTenant(name: String, domain: String, customDomain: String?): RepositoryResult<AddTenantResponse>
}

class TenantsRepositoryImplementation(
    private val networkManager: NetworkManager,
    private val dispatcher: CoroutineDispatcher
) : TenantsRepository {

    override suspend fun getTenants(): RepositoryResult<List<Tenant>> = withContext(dispatcher) {
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
                    RepositoryResult.Success(response.data)
                }

                is NetworkResult.Error -> {
                    RepositoryResult.Failure(response.message)
                }

                is NetworkResult.Exception -> {
                    RepositoryResult.Failure(response.throwable.message ?: "Failed to fetch tenants")
                }
            }
        } catch (e: Exception) {
            RepositoryResult.Failure(e.message ?: "An unexpected error occurred")
        }
    }

    override suspend fun createTenant(
        name: String,
        domain: String,
        customDomain: String?
    ): RepositoryResult<AddTenantResponse> = withContext(dispatcher) {
        val networkResult = networkManager.postJson<AddTenantRequest, AddTenantResponse>(
            "/tenants",
            AddTenantRequest(name, domain, customDomain),
            options = RequestOptions(isAuthRequired = true, isTenantRequired = false)
        )
        when(networkResult) {
            is NetworkResult.Success -> RepositoryResult.Success(networkResult.data)
            is NetworkResult.Error -> RepositoryResult.Failure(networkResult.message)
            is NetworkResult.Exception -> RepositoryResult.Failure(networkResult.throwable.message ?: "Failed to create tenant")
        }
    }

}

@Serializable
data class AddTenantRequest(
    val name: String,
    val subDomain: String,
    val customDomain: String?
)

@Serializable
data class AddTenantResponse(
    val tenantId: Int,
    val subDomain: String,
    val customDomain: String?,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val rowVersion: String
)