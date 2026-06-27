package org.example.asw_portal_kmp.network.api

sealed class RepositoryResult<out T> {
    data class Success<out T>(val data: T) : RepositoryResult<T>()
    data class Failure(val message: String) : RepositoryResult<Nothing>()
}