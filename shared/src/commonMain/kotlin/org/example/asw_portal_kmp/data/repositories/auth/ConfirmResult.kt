package org.example.asw_portal_kmp.data.repositories.auth

sealed class ConfirmResult {
    object Success : ConfirmResult()
    data class Failure(val error: String) : ConfirmResult()
}