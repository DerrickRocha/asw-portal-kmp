package org.example.asw_portal_kmp.network.responses

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    private val accessToken: String,
    private val idToken: String,
    private val refreshToken: String
)
