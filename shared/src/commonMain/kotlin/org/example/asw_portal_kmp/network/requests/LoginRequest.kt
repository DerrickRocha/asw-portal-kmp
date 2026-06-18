package org.example.asw_portal_kmp.network.requests

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String) {
}