package org.example.asw_portal_kmp.network.api.tenants

import org.example.asw_portal_kmp.ui.viewModels.Tenant

interface TenantsRepository {

    suspend fun getTenants(): Result<List<Tenant>>
    suspend fun createTenant(name: String, domain: String, customDomain: String?): Result<Tenant>
}