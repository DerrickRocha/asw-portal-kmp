package org.example.asw_portal_kmp.data.repositories.tenants

import kotlinx.serialization.Serializable

@Serializable
data class NetworkTenant(
    val tenantId: Int,
    val name: String,
    val subDomain: String,
    val customDomain: String?,
    val createdAt: String,
    val updatedAt: String,
    val rowVersion: String
)
