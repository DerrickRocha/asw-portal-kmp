package org.example.asw_portal_kmp.data.repositories.tenants

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.example.asw_portal_kmp.data.database.TenantEntity
import org.example.asw_portal_kmp.data.database.TenantsDao
import org.example.asw_portal_kmp.data.models.Tenant
import org.example.asw_portal_kmp.data.network.NetworkManager
import org.example.asw_portal_kmp.data.network.NetworkResult
import org.example.asw_portal_kmp.data.repositories.RepositoryResult
import org.example.asw_portal_kmp.data.network.RequestOptions
import org.example.asw_portal_kmp.data.network.tenants.NetworkTenant
import org.example.asw_portal_kmp.data.network.deleteJson
import org.example.asw_portal_kmp.data.network.getJson
import org.example.asw_portal_kmp.data.network.postJson
import org.example.asw_portal_kmp.data.network.putJson
import org.example.asw_portal_kmp.data.schedulers.BackgroundConstraints
import org.example.asw_portal_kmp.data.schedulers.BackgroundScheduler
import org.example.asw_portal_kmp.utils.DateUtils.needsUpdate
import kotlin.time.Clock

interface TenantsRepository {

    val tenants: Flow<List<Tenant>>

    suspend fun syncTenants()

    suspend fun executeNetworkSync()

    suspend fun createTenant(name: String, domain: String, customDomain: String?): RepositoryResult<AddTenantResponse>

    suspend fun getTenant(id: Int): RepositoryResult<NetworkTenant>

    suspend fun deleteTenant(id: Int): RepositoryResult<Unit>
    suspend fun updateTenant(updatedNetworkTenant: Tenant): RepositoryResult<Unit>
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


class TenantsRepositoryImplementation(
    private val networkManager: NetworkManager,
    private val dispatcher: CoroutineDispatcher,
    private val tenantsDao: TenantsDao,
    private val scheduler: BackgroundScheduler,
) : TenantsRepository {

    override val tenants: Flow<List<Tenant>> =
        tenantsDao.getAllAsFlow().map { entities ->
            entities.map(TenantEntity::toTenant)
        }
            .distinctUntilChanged()
            .flowOn(dispatcher)

    override suspend fun syncTenants() = withContext(dispatcher) {
        val dbTenants = tenantsDao.getAll()
        if (dbTenants.isEmpty()) {
            executeNetworkSync()
        } else {
            exponentialTenantsSync()
        }
    }

    private suspend fun exponentialTenantsSync() = withContext(dispatcher) {
        if (tenantsDao.getLastModified().needsUpdate(10)) {
            scheduler.schedulePeriodicTask(
                taskId = "tenants",
                workerName = "tenants",
                intervalMs = (60 * 1000L) * 15,
                constraints = BackgroundConstraints(
                    requiresNetwork = true,
                    requiresCharging = false
                )
            )
        }
    }

    override suspend fun executeNetworkSync() = withContext(dispatcher) {
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

                is NetworkResult.Error -> {
                    throw Exception(response.message)
                }


                is NetworkResult.Exception -> {
                    throw Exception(
                        response.throwable.message ?: "Failed to fetch tenants"
                    )
                }
            }

        } catch (exception: Exception) {
            throw Exception(exception.message ?: "Failed to fetch tenants")
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

    override suspend fun deleteTenant(id: Int): RepositoryResult<Unit> = withContext(dispatcher) {
        val networkResult = networkManager.deleteJson<Int>(
            url = "/tenants/$id",
            options = RequestOptions(isAuthRequired = true, isTenantRequired = false)
        )
        when (networkResult) {
            is NetworkResult.Success -> RepositoryResult.Success(Unit)
            is NetworkResult.Error -> RepositoryResult.Failure(networkResult.message)
            is NetworkResult.Exception -> RepositoryResult.Failure(
                networkResult.throwable.message ?: "Failed to delete tenant"
            )
        }
    }

    override suspend fun updateTenant(updatedNetworkTenant: Tenant): RepositoryResult<Unit> = withContext(dispatcher) {
        val networkResult = networkManager.putJson<Tenant, NetworkTenant>(
            url = "/tenants",
            options = RequestOptions(isAuthRequired = true, false),
            requestBody = updatedNetworkTenant
        )
        when (networkResult) {
            is NetworkResult.Success -> RepositoryResult.Success(Unit)
            is NetworkResult.Error -> RepositoryResult.Failure(networkResult.message)
            is NetworkResult.Exception -> RepositoryResult.Failure(
                networkResult.throwable.message ?: "Failed to update tenant"
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