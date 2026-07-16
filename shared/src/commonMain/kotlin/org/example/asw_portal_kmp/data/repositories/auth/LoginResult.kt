package org.example.asw_portal_kmp.data.repositories.auth

sealed interface LoginResult {
    data object Success : LoginResult
    data class Failure(val error: String) : LoginResult
}