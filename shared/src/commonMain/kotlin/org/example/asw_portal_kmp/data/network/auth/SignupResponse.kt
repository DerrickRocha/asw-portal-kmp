package org.example.asw_portal_kmp.data.network.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignupResponse(
    val tenantId: Int,
    val userId: Int,
    val cognitoSub: String,
    val userConfirmed: Boolean,
    val requiresEmailConfirmation: Boolean
)
