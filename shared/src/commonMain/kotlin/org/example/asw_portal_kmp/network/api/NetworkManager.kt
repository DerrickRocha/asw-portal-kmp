package org.example.asw_portal_kmp.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import io.ktor.http.headers

data class RequestOptions(val isAuthRequired: Boolean = false, val isTenantRequired: Boolean = false, val timeout: Long? = 10000)

class NetworkManager(val client: HttpClient) {

    private fun getHeaders() = headers {

    }

    suspend fun get(url: String, params: Map<String, String>, options: RequestOptions) {
        val response = client.request(url) {
            method = HttpMethod.Get
            getHeaders()
        }
    }
}