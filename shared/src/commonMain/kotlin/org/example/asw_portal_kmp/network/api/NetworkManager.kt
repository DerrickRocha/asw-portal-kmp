package org.example.asw_portal_kmp.network.api

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.headers
import kotlinx.io.IOException
import org.example.asw_portal_kmp.data.KeyValuePairManager

class AuthenticationException(message: String) : IOException(message)
class TenantException(message: String) : IOException(message)
data class RequestOptions(
    val isAuthRequired: Boolean = false,
    val isTenantRequired: Boolean = false,
    val timeout: Long? = 10000
)

class NetworkManager(
    private val client: HttpClient,
    private val manager: KeyValuePairManager
) {

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_TENANT_ID = "X-Tenant-Id"
        private const val TOKEN_PREFIX = "Bearer "
    }

    private suspend fun buildHeaders(
        isAuthRequired: Boolean,
        isTenantRequired: Boolean
    ): Map<String, String> {
        val headers = mutableMapOf<String, String>()

        if (isAuthRequired) {
            val token = manager.getIdToken()
                ?: throw AuthenticationException("Authentication required but no token found in DataStore")
            headers[HEADER_AUTHORIZATION] = "$TOKEN_PREFIX$token"
        }

        if (isTenantRequired) {
            val tenantId = manager.getTenantId()
                ?: throw TenantException("Tenant ID required but no tenant ID found in DataStore")
            headers[HEADER_TENANT_ID] = tenantId
        }

        return headers
    }

    suspend fun <T>get(url: String, params: Map<String, String>, options: RequestOptions): Result<T> {

        return runCatching {
            val headerMap = buildHeaders(options.isAuthRequired, options.isTenantRequired)

            val response = client.request(url) {
                method = HttpMethod.Get
                contentType(ContentType.Application.Json)
                headers {
                    headerMap.forEach { (key, value) ->
                        append(key, value)
                    }
                }
                url {
                    params.forEach { (key, value) ->
                        parameters.append(key, value)
                    }
                }
                timeout {
                    requestTimeoutMillis = options.timeout ?: 10000
                }
            }
            if (response.status.value in 200..299) return response.body()
            else throw Exception("Error: ${response.status.value} - ${response.bodyAsText()}")
        }

    }
}