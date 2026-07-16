package org.example.asw_portal_kmp.data.network.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken: String,
    val idToken: String,
    val refreshToken: String
)
