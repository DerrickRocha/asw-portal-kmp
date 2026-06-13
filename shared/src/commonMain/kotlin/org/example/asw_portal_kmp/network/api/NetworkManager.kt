package org.example.asw_portal_kmp.network.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.headers
import kotlinx.io.IOException
import org.example.asw_portal_kmp.data.KeyValuePairManager

class AuthenticationException(message: String) : IOException(message)
class TenantException(message: String) : IOException(message)
class JsonParsingException(message: String, cause: Throwable) : IOException(message, cause)

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T, val statusCode: Int = 200, val headers: Headers = Headers.Empty) :
        NetworkResult<T>()

    data class Error(
        val message: String,
        val statusCode: Int? = null,
        val errorBody: String? = null
    ) : NetworkResult<Nothing>()

    data class Exception(val throwable: kotlin.Exception) : NetworkResult<Nothing>()
}

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

    suspend fun <T> get(
        url: String,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        deserialize: (String) -> T
    ): NetworkResult<T> {

        return try {
            val headerMap = buildHeaders(options.isAuthRequired, options.isTenantRequired)

            val response = client.request(url) {
                method = HttpMethod.Get
                contentType(ContentType.Application.Json)
                headers { headerMap.forEach { (key, value) -> append(key, value) } }
                url { params.forEach { (key, value) -> parameters.append(key, value) } }
                timeout { requestTimeoutMillis = options.timeout ?: 10000 }
            }

            val body = response.bodyAsText()
            val statusCode = response.status.value

            if (statusCode in 200..299) {
                try {
                    val data = deserialize(body)
                    NetworkResult.Success(data, statusCode, response.headers)
                } catch (e: Exception) {
                    NetworkResult.Exception(JsonParsingException("Failed to parse response: ${e.message}", e))
                }
            } else {
                NetworkResult.Error(
                    message = "HTTP $statusCode: ${response.status.description}",
                    statusCode = statusCode,
                    errorBody = body
                )
            }
        } catch (e: Exception) {
            when (e) {
                is AuthenticationException, is TenantException -> NetworkResult.Error(e.message ?: "Auth error")
                else -> NetworkResult.Exception(e)
            }
        }
    }
}