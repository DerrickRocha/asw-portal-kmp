package org.example.asw_portal_kmp.network.api.tenants

import kotlinx.serialization.Serializable

@Serializable
data class Tenant(
    val tenantId: Int,
    val name: String,
    val subDomain: String,
    val customDomain: String?,
    val createdAt: String,
    val updatedAt: String,
    val rowVersion: String
)
