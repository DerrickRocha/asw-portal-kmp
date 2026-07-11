package org.example.asw_portal_kmp.data.repositories.tenants

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.example.asw_portal_kmp.data.database.tenants.TenantDao
import org.example.asw_portal_kmp.data.database.tenants.TenantEntity
import org.example.asw_portal_kmp.data.models.Tenant
import org.example.asw_portal_kmp.network.NetworkManager
import org.example.asw_portal_kmp.network.NetworkResult
import org.example.asw_portal_kmp.data.repositories.RepositoryResult
import org.example.asw_portal_kmp.network.RequestOptions
import org.example.asw_portal_kmp.network.getJson
import org.example.asw_portal_kmp.network.postJson
import org.example.asw_portal_kmp.utils.DateUtils.needsUpdate
import kotlin.time.Clock

interface TenantsRepository {

    val tenants: Flow<List<Tenant>>

    suspend fun syncTenants()

    suspend fun executeNetworkSync()

    suspend fun getTenants(): RepositoryResult<List<NetworkTenant>>
    suspend fun createTenant(name: String, domain: String, customDomain: String?): RepositoryResult<AddTenantResponse>
}

fun TenantEntity.toTenant(): Tenant {
    return Tenant(
        this.id,
        this.name,
        this.subDomain,
        this.customDomain,
        this.createdAt,
        this.updatedAt,
        this.rowVersion
    )
}

class TenantsRepositoryImplementation(
    private val networkManager: NetworkManager,
    private val dispatcher: CoroutineDispatcher,
    private val tenantsDao: TenantDao
) : TenantsRepository {

    override val tenants: Flow<List<Tenant>> =
        tenantsDao.getAllAsFlow().map { entities -> entities.map(TenantEntity::toTenant) }

    override suspend fun syncTenants() = withContext(dispatcher) {
        val dbTenants = tenantsDao.getAll()
        if (dbTenants.isEmpty()) {
            executeNetworkSync()
        } else {
            exponentialTenantsSync()
        }
    }

    private fun exponentialTenantsSync() {
        if (tenantsDao.getLastModified().needsUpdate(10)) {
            //
        }
    }

    override suspend fun executeNetworkSync() {
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
                    val entities = response.data.map { it.toEntity() }
                    tenantsDao.insertAll(entities)
                }

                is NetworkResult.Error -> throw Exception(response.message)


                is NetworkResult.Exception -> throw Exception(
                    response.throwable.message ?: "Failed to fetch tenants"
                )
            }

        } catch (exception: Exception) {
            throw Exception(exception.message ?: "Failed to fetch tenants")
        }
    }

    fun NetworkTenant.toEntity(): TenantEntity {
        return TenantEntity(
            id = this.tenantId,
            name = this.name,
            subDomain = this.subDomain,
            customDomain = this.customDomain,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            rowVersion = this.rowVersion,
            isDone = true,
            lastModified = Clock.System.now().epochSeconds
        )
    }


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