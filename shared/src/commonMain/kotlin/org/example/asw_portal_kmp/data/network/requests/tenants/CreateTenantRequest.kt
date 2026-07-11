package org.example.asw_portal_kmp.data.network.requests.tenants

import kotlinx.serialization.Serializable

@Serializable
data class CreateTenantRequest(
    val name: String,
    val subDomain: String,
    val customDomain: String?
)
