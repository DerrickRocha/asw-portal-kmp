package org.example.asw_portal_kmp.data.network.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String) {
}