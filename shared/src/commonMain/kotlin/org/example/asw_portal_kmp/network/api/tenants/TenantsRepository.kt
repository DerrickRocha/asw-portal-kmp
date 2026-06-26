package org.example.asw_portal_kmp.network.api.tenants

import org.example.asw_portal_kmp.network.NetworkManager
import org.example.asw_portal_kmp.ui.viewModels.Tenant

interface TenantsRepository {

    suspend fun getTenants(): Result<List<Tenant>>
    suspend fun createTenant(name: String, domain: String, customDomain: String?): Result<Tenant>
}

class TenantsRepositoryImplementation(private val networkManager: NetworkManager): TenantsRepository {
    override suspend fun getTenants(): Result<List<Tenant>> {
        TODO("Not yet implemented")
    }

    override suspend fun createTenant(
        name: String,
        domain: String,
        customDomain: String?
    ): Result<Tenant> {
        TODO("Not yet implemented")
    }

}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val message: String) : Result<Nothing>()
}