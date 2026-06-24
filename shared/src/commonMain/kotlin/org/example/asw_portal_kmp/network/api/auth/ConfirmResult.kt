package org.example.asw_portal_kmp.network.api.auth

sealed class ConfirmResult {
    object Success : ConfirmResult()
    data class Failure(val error: String) : ConfirmResult()
}