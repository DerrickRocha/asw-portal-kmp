package org.example.asw_portal_kmp.data.models

data class Tenant(
    val id: Int,
    val name: String,
    val subDomain: String,
    val customDomain: String?,
    val createdAt: String,
    val updatedAt: String,
    val rowVersion: String,
)