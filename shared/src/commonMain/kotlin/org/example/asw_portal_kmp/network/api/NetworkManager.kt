package org.example.asw_portal_kmp.network.api

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.headers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.io.IOException

class AuthenticationException(message: String) : IOException(message)
class TenantException(message: String) : IOException(message)
data class RequestOptions(
    val isAuthRequired: Boolean = false,
    val isTenantRequired: Boolean = false,
    val timeout: Long? = 10000
)

class NetworkManager(
    private val client: HttpClient,
    private val store: DataStore<Preferences>
) {

    companion object {
        // Preference keys
        private val KEY_ID_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_TENANT_ID = stringPreferencesKey("tenant_id")

        // Header names
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_TENANT_ID = "X-Tenant-Id"
        private const val TOKEN_PREFIX = "Bearer "
    }

    private suspend fun getAuthToken(): String? {
        return store.data.map { preferences ->
            preferences[KEY_ID_TOKEN]
        }.first()
    }

    private suspend fun getTenantId(): String? {
        return store.data.map { preferences ->
            preferences[KEY_TENANT_ID]
        }.first()
    }

    private suspend fun buildHeaders(
        isAuthRequired: Boolean,
        isTenantRequired: Boolean
    ): Map<String, String> {
        val headers = mutableMapOf<String, String>()

        if (isAuthRequired) {
            val token = getAuthToken()
                ?: throw AuthenticationException("Authentication required but no token found in DataStore")
            headers[HEADER_AUTHORIZATION] = "$TOKEN_PREFIX$token"
        }

        if (isTenantRequired) {
            val tenantId = getTenantId()
                ?: throw TenantException("Tenant ID required but no tenant ID found in DataStore")
            headers[HEADER_TENANT_ID] = tenantId
        }

        return headers
    }

    suspend fun get(url: String, params: Map<String, String>, options: RequestOptions): String {
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
        val body = response.bodyAsText()
        return body
    }
}