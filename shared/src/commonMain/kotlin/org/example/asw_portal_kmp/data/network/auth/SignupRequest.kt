package org.example.asw_portal_kmp.data.network.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    val companyName: String,
    val email: String,
    val password: String,
    val subdomain: String,
    val customDomain: String?
)