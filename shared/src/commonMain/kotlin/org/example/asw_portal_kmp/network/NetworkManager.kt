package org.example.asw_portal_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecodingException
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
            if(token.isNullOrBlank()) throw AuthenticationException("Authentication required but no token found in DataStore")
            headers[HEADER_AUTHORIZATION] = "$TOKEN_PREFIX$token"
        }

        if (isTenantRequired) {
            val tenantId = manager.getTenantId()
                ?: throw TenantException("Tenant ID required but no tenant ID found in DataStore")
            headers[HEADER_TENANT_ID] = tenantId
        }

        return headers
    }

    // Generic request method to avoid code duplication
    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun <T, R> executeRequest(
        method: HttpMethod,
        url: String,
        requestBody: T? = null,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        serialize: (T) -> String,
        deserialize: (String) -> R
    ): NetworkResult<R> {
        return try {
            val headerMap = buildHeaders(options.isAuthRequired, options.isTenantRequired)

            val response = client.request(url) {
                this.method = method
                contentType(ContentType.Application.Json)
                if (headerMap.isNotEmpty()) {
                    headerMap.forEach { (key, value) ->
                        header(key, value)
                    }
                }

                if (params.isNotEmpty()) {
                    url {
                        params.forEach { (key, value) ->
                            parameters.append(key, value)
                        }
                    }
                }
                timeout { requestTimeoutMillis = options.timeout ?: 10000 }

                requestBody?.let {
                    val jsonString = serialize(it)
                    setBody(jsonString)
                }
            }

            val body = response.bodyAsText()
            val statusCode = response.status.value

            if (statusCode in 200..299) {
                val data = deserialize(body)
                NetworkResult.Success(data, statusCode, response.headers)
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
                is JsonDecodingException -> NetworkResult.Exception(JsonParsingException(e.message ?: "JSON parsing error", e))
                else -> NetworkResult.Exception(e)
            }
        }
    }

    // GET method
    suspend fun <T> get(
        url: String,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        deserialize: (String) -> T
    ): NetworkResult<T> {
        return executeRequest(
            method = HttpMethod.Get,
            url = url,
            requestBody = null,
            params = params,
            options = options,
            serialize = { "" },
            deserialize = deserialize
        )
    }

    // POST method
    suspend fun <T, R> post(
        url: String,
        requestBody: T,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        serialize: (T) -> String,
        deserialize: (String) -> R
    ): NetworkResult<R> {
        return executeRequest(
            method = HttpMethod.Post,
            url = url,
            requestBody = requestBody,
            params = params,
            options = options,
            serialize = serialize,
            deserialize = deserialize
        )
    }

    // POST without response body
    suspend fun post(
        url: String,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions()
    ): NetworkResult<Unit> {
        return post(
            url = url,
            requestBody = Unit,
            params = params,
            options = options,
            serialize = { "" },
            deserialize = { }
        )
    }

    // PUT method
    suspend fun <T, R> put(
        url: String,
        requestBody: T,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        serialize: (T) -> String,
        deserialize: (String) -> R
    ): NetworkResult<R> {
        return executeRequest(
            method = HttpMethod.Put,
            url = url,
            requestBody = requestBody,
            params = params,
            options = options,
            serialize = serialize,
            deserialize = deserialize
        )
    }

    // PUT without response body
    suspend fun put(
        url: String,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions()
    ): NetworkResult<Unit> {
        return put(
            url = url,
            requestBody = Unit,
            params = params,
            options = options,
            serialize = { "" },
            deserialize = { }
        )
    }

    // PATCH method
    suspend fun <T, R> patch(
        url: String,
        requestBody: T,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        serialize: (T) -> String,
        deserialize: (String) -> R
    ): NetworkResult<R> {
        return executeRequest(
            method = HttpMethod.Patch,
            url = url,
            requestBody = requestBody,
            params = params,
            options = options,
            serialize = serialize,
            deserialize = deserialize
        )
    }

    // PATCH without response body
    suspend fun patch(
        url: String,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions()
    ): NetworkResult<Unit> {
        return patch(
            url = url,
            requestBody = Unit,
            params = params,
            options = options,
            serialize = { "" },
            deserialize = { }
        )
    }

    // DELETE method (typically no request body)
    suspend fun <R> delete(
        url: String,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        deserialize: (String) -> R
    ): NetworkResult<R> {
        return executeRequest(
            method = HttpMethod.Delete,
            url = url,
            requestBody = null,
            params = params,
            options = options,
            serialize = { "" },
            deserialize = deserialize
        )
    }

    // DELETE without response body
    suspend fun delete(
        url: String,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions()
    ): NetworkResult<Unit> {
        return delete(
            url = url,
            params = params,
            options = options,
            deserialize = { }
        )
    }

    // DELETE with request body (less common but sometimes needed)
    suspend fun <T, R> deleteWithBody(
        url: String,
        requestBody: T,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        serialize: (T) -> String,
        deserialize: (String) -> R
    ): NetworkResult<R> {
        return executeRequest(
            method = HttpMethod.Delete,
            url = url,
            requestBody = requestBody,
            params = params,
            options = options,
            serialize = serialize,
            deserialize = deserialize
        )
    }

    // Convenience methods with JSON serialization (using kotlinx.serialization)
    suspend inline fun <reified T, reified R> getJson(
        url: String,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        noinline deserialize: (String) -> R = { Json.decodeFromString<R>(it) }
    ): NetworkResult<R> {
        return get(url, params, options, deserialize)
    }

    suspend inline fun <reified T, reified R> postJson(
        url: String,
        requestBody: T,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        noinline serialize: (T) -> String = { Json.encodeToString(it) },
        noinline deserialize: (String) -> R = { Json.decodeFromString<R>(it) }
    ): NetworkResult<R> {
        return post(url, requestBody, params, options, serialize, deserialize)
    }

    suspend inline fun <reified T, reified R> putJson(
        url: String,
        requestBody: T,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        noinline serialize: (T) -> String = { Json.encodeToString(it) },
        noinline deserialize: (String) -> R = { Json.decodeFromString<R>(it) }
    ): NetworkResult<R> {
        return put(url, requestBody, params, options, serialize, deserialize)
    }

    suspend inline fun <reified T, reified R> patchJson(
        url: String,
        requestBody: T,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        noinline serialize: (T) -> String = { Json.encodeToString(it) },
        noinline deserialize: (String) -> R = { Json.decodeFromString<R>(it) }
    ): NetworkResult<R> {
        return patch(url, requestBody, params, options, serialize, deserialize)
    }

    suspend inline fun <reified R> deleteJson(
        url: String,
        params: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions(),
        noinline deserialize: (String) -> R = { Json.decodeFromString<R>(it) }
    ): NetworkResult<R> {
        return delete(url, params, options, deserialize)
    }
}