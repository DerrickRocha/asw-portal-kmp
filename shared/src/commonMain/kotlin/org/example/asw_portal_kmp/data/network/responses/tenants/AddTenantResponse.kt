package org.example.asw_portal_kmp.data.network.responses.tenants

import kotlinx.serialization.Serializable

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