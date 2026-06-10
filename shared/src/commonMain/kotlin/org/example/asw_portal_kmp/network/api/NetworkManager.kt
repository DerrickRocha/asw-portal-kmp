package org.example.asw_portal_kmp.network.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.headers

data class RequestOptions(val isAuthRequired: Boolean = false, val isTenantRequired: Boolean = false, val timeout: Long? = 10000)

class NetworkManager(val client: HttpClient) {

    private fun getHeaders(isAuthRequired: Boolean, isTenantRequired: Boolean) = headers {
        if (isAuthRequired) {
            // get token
            // throw error if token not found
            // add token to header
        }

        if (isTenantRequired) {
            // get tenantId
            // throw error if tenantId not found
            // add tenantId to header (X-Tenant-Id).
        }
    }

    suspend fun get(url: String, params: Map<String, String>, options: RequestOptions): String {
        val response = client.request(url) {
            method = HttpMethod.Get
            contentType(ContentType.Application.Json)
            headers {
                getHeaders(options.isAuthRequired, options.isTenantRequired)
            }
            url {
                params.forEach { (key, value) ->
                    parameters.append(key, value)
                }
            }
        }
        val body = response.bodyAsText()
        return body
    }
}