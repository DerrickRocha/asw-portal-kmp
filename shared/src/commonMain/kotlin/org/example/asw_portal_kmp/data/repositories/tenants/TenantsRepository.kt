package org.example.asw_portal_kmp.data.repositories.tenants

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.example.asw_portal_kmp.data.network.NetworkManager
import org.example.asw_portal_kmp.data.network.NetworkResult
import org.example.asw_portal_kmp.data.repositories.RepositoryResult
import org.example.asw_portal_kmp.data.network.RequestOptions
import org.example.asw_portal_kmp.data.network.tenants.NetworkTenant
import org.example.asw_portal_kmp.data.network.deleteJson
import org.example.asw_portal_kmp.data.network.getJson
import org.example.asw_portal_kmp.data.network.postJson
import org.example.asw_portal_kmp.data.network.putJson

interface TenantsRepository {

    suspend fun getTenants(): RepositoryResult<List<NetworkTenant>>
    suspend fun createTenant(name: String, domain: String, customDomain: String?): RepositoryResult<AddTenantResponse>

    suspend fun getTenant(id: Int): RepositoryResult<NetworkTenant>

    suspend fun deleteTenant(id: Int): RepositoryResult<Unit>
    suspend fun updateTenant(updatedNetworkTenant: NetworkTenant): RepositoryResult<Unit>
}

class TenantsRepositoryImplementation(
    private val networkManager: NetworkManager,
    private val dispatcher: CoroutineDispatcher
) : TenantsRepository {

    override suspend fun getTenants(): RepositoryResult<List<NetworkTenant>> = withContext(dispatcher) {
        try {
            val response = networkManager.getJson<List<NetworkTenant>>(
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
        when (networkResult) {
            is NetworkResult.Success -> RepositoryResult.Success(networkResult.data)
            is NetworkResult.Error -> RepositoryResult.Failure(networkResult.message)
            is NetworkResult.Exception -> RepositoryResult.Failure(
                networkResult.throwable.message ?: "Failed to create tenant"
            )
        }
    }

    override suspend fun getTenant(id: Int): RepositoryResult<NetworkTenant> = withContext(dispatcher) {
        val networkResult = networkManager.getJson<NetworkTenant>(
            "/tenants/$id",
            options = RequestOptions(isAuthRequired = true, isTenantRequired = false)
        )
        when (networkResult) {
            is NetworkResult.Success -> RepositoryResult.Success(networkResult.data)
            is NetworkResult.Error -> RepositoryResult.Failure(networkResult.message)
            is NetworkResult.Exception -> RepositoryResult.Failure(
                networkResult.throwable.message ?: "Failed to fetch tenant"
            )
        }
    }

    override suspend fun deleteTenant(id: Int): RepositoryResult<Unit> {
        val networkResult = networkManager.deleteJson<Int>(url = "/tenants/$id", options = RequestOptions(isAuthRequired = true, isTenantRequired = false))
        return when (networkResult) {
            is NetworkResult.Success -> RepositoryResult.Success(Unit)
            is NetworkResult.Error -> RepositoryResult.Failure(networkResult.message)
            is NetworkResult.Exception -> RepositoryResult.Failure(networkResult.throwable.message ?: "Failed to delete tenant")
        }
    }

    override suspend fun updateTenant(updatedNetworkTenant: NetworkTenant): RepositoryResult<Unit> {
        val networkResult = networkManager.putJson<NetworkTenant, NetworkTenant>(url = "/tenants", options = RequestOptions(isAuthRequired = true, false), requestBody = updatedNetworkTenant)
        return when (networkResult) {
            is NetworkResult.Success -> RepositoryResult.Success(Unit)
            is NetworkResult.Error -> RepositoryResult.Failure(networkResult.message)
            is NetworkResult.Exception -> RepositoryResult.Failure(networkResult.throwable.message ?: "Failed to update tenant")
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