package org.example.asw_portal_kmp.data.schedulers

sealed class BackgroundResult {
    data class Success(val message: String? = null) : BackgroundResult()
    data class Failure(val error: String) : BackgroundResult()
    object Retry : BackgroundResult()
}