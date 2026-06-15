package org.example.asw_portal_kmp.network

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecodingException
import org.example.asw_portal_kmp.data.KeyValuePairManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkManagerTest {
    @Serializable
    data class TestUser(val id: Int, val name: String, val email: String)

    @Serializable
    data class CreateUserRequest(val name: String, val email: String)

    @Serializable
    data class UpdateUserRequest(val name: String, val email: String)

    @Serializable
    data class DeleteOptions(val hardDelete: Boolean, val reason: String)

    private lateinit var networkManager: NetworkManager
    private val mockKeyValueManager = mock<KeyValuePairManager>(mode = MockMode.autofill)
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeTest
    fun setup() {
        val mockEngine = MockEngine { request ->
            // Handle timeout simulation
            if (request.url.toString().contains("/timeout")) {
                delay(500.milliseconds)
                respond(
                    content = "",
                    status = HttpStatusCode.OK,
                    headers = request.headers
                )
            } else {
                respond(
                    content = getMockResponseForRequest(request),
                    status = getMockStatusCodeForRequest(request),
                    headers = request.headers
                )
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        networkManager = NetworkManager(client, mockKeyValueManager)
    }

    // MARK: - GET Tests

    @Test
    fun testGetSuccess() = runTest {
        // When
        val result = networkManager.get(
            url = "https://api.example.com/users/1",
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals(1, success.data.id)
        assertEquals("John", success.data.name)
        assertEquals(200, success.statusCode)
    }

    @Test
    fun testGetWithQueryParams() = runTest {
        // When
        val result = networkManager.get<List<TestUser>>(
            url = "https://api.example.com/users",
            params = mapOf("page" to "1", "limit" to "10"),
            deserialize = { json.decodeFromString<List<TestUser>>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        // The test passes if we get a success response (the mock engine handled the params)
    }

    @Test
    fun testGetWithAuthentication() = runTest {
        // Given
        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token-123"

        // When
        val result = networkManager.get<TestUser>(
            url = "https://api.example.com/users/1",
            options = RequestOptions(isAuthRequired = true),
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        verifySuspend { mockKeyValueManager.getIdToken() }
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun testGetWithTenantRequired() = runTest {
        // Given
        everySuspend { mockKeyValueManager.getTenantId() } returns "tenant-456"

        // When
        val result = networkManager.get<TestUser>(
            url = "https://api.example.com/users/1",
            options = RequestOptions(isTenantRequired = true),
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        verifySuspend { mockKeyValueManager.getTenantId() }
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun testGetWithMissingAuthToken() = runTest {
        // Given
        everySuspend { mockKeyValueManager.getIdToken() } returns null

        // When
        val result = networkManager.get<TestUser>(
            url = "https://api.example.com/users/1",
            options = RequestOptions(isAuthRequired = true),
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.message?.contains("Authentication required") == true)
    }

    @Test
    fun testGetWithMissingTenantId() = runTest {
        // Given
        everySuspend { mockKeyValueManager.getTenantId() } returns null

        // When
        val result = networkManager.get<TestUser>(
            url = "https://api.example.com/users/1",
            options = RequestOptions(isTenantRequired = true),
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.message?.contains("Tenant ID required") == true)
    }

    @Test
    fun testGetWithBothAuthAndTenant() = runTest {
        // Given
        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token"
        everySuspend { mockKeyValueManager.getTenantId() } returns "test-tenant"

        // When
        val result = networkManager.get<TestUser>(
            url = "https://api.example.com/users/1",
            options = RequestOptions(isAuthRequired = true, isTenantRequired = true),
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        verifySuspend {
            mockKeyValueManager.getIdToken()
            mockKeyValueManager.getTenantId()
        }
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun testGetWithNotFoundError() = runTest {
        // When
        val result = networkManager.get<TestUser>(
            url = "https://api.example.com/users/999",
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertEquals(404, error.statusCode)
    }

    @Test
    fun testGetWithServerError() = runTest {
        // When
        val result = networkManager.get<TestUser>(
            url = "https://api.example.com/users/error",
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertEquals(500, error.statusCode)
    }

    // MARK: - POST Tests

    @Test
    fun testPostSuccess() = runTest {
        // Given
        val request = CreateUserRequest("Jane Doe", "jane@example.com")

        // When
        val result = networkManager.post(
            url = "https://api.example.com/users",
            requestBody = request,
            serialize = { json.encodeToString(it) },
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals(2, success.data.id)
        assertEquals("Jane Doe", success.data.name)
        assertEquals(201, success.statusCode)
    }

    @Test
    fun testPostWithoutResponseBody() = runTest {
        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token"
        // When
        val result = networkManager.post(
            url = "https://api.example.com/users/1/activate",
            options = RequestOptions(isAuthRequired = true)
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals(Unit, success.data)
        assertEquals(204, success.statusCode)
    }

    @Test
    fun testPostWithAuthentication() = runTest {
        // Given
        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token"
        val request = CreateUserRequest("John", "john@example.com")

        // When
        val result = networkManager.post(
            url = "https://api.example.com/users",
            requestBody = request,
            options = RequestOptions(isAuthRequired = true),
            serialize = { json.encodeToString(it) },
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        verifySuspend { mockKeyValueManager.getIdToken() }
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun testPostWithBadRequest() = runTest {
        // Given
        val request = CreateUserRequest("", "")  // Invalid request

        // When
        val result = networkManager.post(
            url = "https://api.example.com/users",
            requestBody = request,
            serialize = { json.encodeToString(it) },
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertEquals(400, error.statusCode)
    }

    // MARK: - PUT Tests

    @Test
    fun testPutSuccess() = runTest {

        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token-123"
        // Given
        val request = UpdateUserRequest("John Updated", "john.updated@example.com")

        // When
        val result = networkManager.put(
            url = "https://api.example.com/users/1",
            options = RequestOptions(isAuthRequired = true),
            requestBody = request,
            serialize = { json.encodeToString(it) },
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals("John Updated", success.data.name)
    }

    @Test
    fun testPutWithoutResponseBody() = runTest {
        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token-123"
        // When
        val result = networkManager.put(
            url = "https://api.example.com/users/1",
            options = RequestOptions(isAuthRequired = true)
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(200, (result as NetworkResult.Success).statusCode)
    }

    @Test
    fun testPutWithAuthentication() = runTest {
        // Given
        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token"
        val request = UpdateUserRequest("John", "john@example.com")

        // When
        val result = networkManager.put(
            url = "https://api.example.com/users/1",
            requestBody = request,
            options = RequestOptions(isAuthRequired = true),
            serialize = { json.encodeToString(it) },
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        verifySuspend { mockKeyValueManager.getIdToken() }
        assertTrue(result is NetworkResult.Success)
    }

    // MARK: - PATCH Tests

    @Test
    fun testPatchSuccess() = runTest {
        // Given
        val partialUpdate = mapOf("name" to "John Patched")

        // When
        val result = networkManager.patch(
            url = "https://api.example.com/users/1",
            requestBody = partialUpdate,
            serialize = { json.encodeToString(it) },
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals("John Patched", success.data.name)
    }

    @Test
    fun testPatchWithoutResponseBody() = runTest {
        // When
        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token-123"
        val result = networkManager.patch(
            url = "https://api.example.com/users/1/touch",
            options = RequestOptions(isAuthRequired = true)
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(204, (result as NetworkResult.Success).statusCode)
    }

    @Test
    fun testPatchWithTenantRequired() = runTest {
        // Given
        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token"
        everySuspend { mockKeyValueManager.getTenantId() } returns "test-tenant"
        val partialUpdate = mapOf("email" to "new@example.com")

        // When
        val result = networkManager.patch(
            url = "https://api.example.com/users/1",
            requestBody = partialUpdate,
            options = RequestOptions(isTenantRequired = true, isAuthRequired = true),
            serialize = { json.encodeToString(it) },
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        verifySuspend { mockKeyValueManager.getTenantId() }
        assertTrue(result is NetworkResult.Success)
    }

    // MARK: - DELETE Tests

    @Test
    fun testDeleteSuccess() = runTest {

        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token-123"
        // When
        val result = networkManager.delete(
            url = "https://api.example.com/users/1",
            options = RequestOptions(isAuthRequired = true)
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(204, (result as NetworkResult.Success).statusCode)
    }

    @Test
    fun testDeleteWithResponseBody() = runTest {
        // When
        val result = networkManager.delete(
            url = "https://api.example.com/users/1",
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals(1, success.data.id)
    }

    @Test
    fun testDeleteWithBody() = runTest {
        // Given
        val options = DeleteOptions(hardDelete = true, reason = "User request")

        // When
        val result = networkManager.deleteWithBody(
            url = "https://api.example.com/users/1",
            requestBody = options,
            serialize = { json.encodeToString(it) },
            deserialize = { Unit }
        )

        // Then
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun testDeleteWithAuthenticationAndTenant() = runTest {
        // Given
        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token"
        everySuspend { mockKeyValueManager.getTenantId() } returns "test-tenant"

        // When
        val result = networkManager.delete(
            url = "https://api.example.com/users/1",
            options = RequestOptions(isAuthRequired = true, isTenantRequired = true)
        )

        // Then
        verifySuspend {
            mockKeyValueManager.getIdToken()
            mockKeyValueManager.getTenantId()
        }
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun testDeleteNotFound() = runTest {
        // When
        val result = networkManager.delete(
            url = "https://api.example.com/users/999",
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertEquals(404, error.statusCode)
    }

    // MARK: - JSON Convenience Method Tests

    @Test
    fun testGetJsonConvenienceMethod() = runTest {
        // When
        val result = networkManager.getJson<TestUser, TestUser>(
            url = "https://api.example.com/users/1"
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals("John", success.data.name)
    }

    @Test
    fun testPostJsonConvenienceMethod() = runTest {
        // Given
        val request = CreateUserRequest("Jane", "jane@example.com")

        // When
        val result = networkManager.postJson<CreateUserRequest, TestUser>(
            url = "https://api.example.com/users",
            requestBody = request
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals("Jane Doe", success.data.name)
    }

    @Test
    fun testPutJsonConvenienceMethod() = runTest {

        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token-123"
        // Given
        val request = UpdateUserRequest("Updated", "updated@example.com")

        // When
        val result = networkManager.putJson<UpdateUserRequest, TestUser>(
            url = "https://api.example.com/users/1",
            requestBody = request,
            options = RequestOptions(isAuthRequired = true)
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals("John Updated", success.data.name)
    }

    @Test
    fun testPatchJsonConvenienceMethod() = runTest {
        // Given
        val request = mapOf("name" to "Patched")

        // When
        val result = networkManager.patchJson<Map<String, String>, TestUser>(
            url = "https://api.example.com/users/1",
            requestBody = request
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals("John Patched", success.data.name)
    }

    @Test
    fun testDeleteJsonConvenienceMethod() = runTest {
        // When
        val result = networkManager.deleteJson<TestUser>(
            url = "https://api.example.com/users/1"
        )

        // Then
        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals(1, success.data.id)
    }

    // MARK: - Error Handling Tests

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testJsonParsingError() = runTest {
        // When
        val result = networkManager.get<TestUser>(
            url = "https://api.example.com/users/invalid-json",
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Exception)
        val exception = result.throwable
        assertTrue(exception is JsonDecodingException)
    }

    @Test
    fun testTimeout() = runTest {
        // When
        val result = networkManager.get<TestUser>(
            url = "https://api.example.com/users/timeout",
            options = RequestOptions(timeout = 100),
            deserialize = { json.decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Exception)
    }

    // MARK: - Helper Functions

    private fun getMockResponseForRequest(request: HttpRequestData): String {
        val url = request.url.toString()
        val method = request.method
        val body = request.body.toString()

        return when {
            // GET responses
            url.contains("/users/1") && method == HttpMethod.Get ->
                """{"id":1,"name":"John","email":"john@example.com"}"""
            url.contains("/users/invalid-json") && method == HttpMethod.Get ->
                "invalid json"
            url.contains("/users/999") && method == HttpMethod.Get -> "{}"
            url.contains("/users/error") && method == HttpMethod.Get -> "{}"
            url.contains("/users") && method == HttpMethod.Get -> "[]"

            // POST responses
            url.contains("/users/1/activate") && method == HttpMethod.Post -> ""
            url.contains("/users") && method == HttpMethod.Post -> {
                if (body.contains("\"name\":\"\"") && body.contains("\"email\":\"\"")) {
                    "{}"  // Bad request case
                } else {
                    """{"id":2,"name":"Jane Doe","email":"jane@example.com"}"""
                }
            }

            // PUT responses
            url.contains("/users/1") && method == HttpMethod.Put -> {
                if (request.headers["Authorization"] != null) {
                    """{"id":1,"name":"John Updated","email":"john.updated@example.com"}"""
                } else {
                    "{}"
                }
            }

            // PATCH responses
            url.contains("/users/1/touch") && method == HttpMethod.Patch -> ""
            url.contains("/users/1") && method == HttpMethod.Patch ->
                """{"id":1,"name":"John Patched","email":"john@example.com"}"""

            // DELETE responses
            url.contains("/users/1") && method == HttpMethod.Delete -> {
                if (url.contains("hardDelete")) {
                    ""  // DELETE with body returns no content
                } else if (request.headers["Authorization"] != null) {
                    ""  // Authenticated DELETE returns no content
                } else {
                    """{"id":1,"name":"John","email":"john@example.com"}"""
                }
            }
            url.contains("/users/999") && method == HttpMethod.Delete -> "{}"

            else -> "{}"
        }
    }

    private fun getMockStatusCodeForRequest(request: HttpRequestData): HttpStatusCode {
        val url = request.url.toString()
        val method = request.method
        val body = request.body.toString()

        return when {
            url.contains("/users/1") && method == HttpMethod.Get -> HttpStatusCode.OK
            url.contains("/users/invalid-json") && method == HttpMethod.Get -> HttpStatusCode.OK
            url.contains("/users/999") && method == HttpMethod.Get -> HttpStatusCode.NotFound
            url.contains("/users/error") && method == HttpMethod.Get -> HttpStatusCode.InternalServerError
            url.contains("/users") && method == HttpMethod.Get -> HttpStatusCode.OK

            url.contains("/users/1/activate") && method == HttpMethod.Post -> HttpStatusCode.NoContent
            url.contains("/users") && method == HttpMethod.Post -> {
                if (body.contains("\"name\":\"\"") && body.contains("\"email\":\"\"")) {
                    HttpStatusCode.BadRequest
                } else {
                    HttpStatusCode.Created
                }
            }

            url.contains("/users/1/touch") && method == HttpMethod.Patch -> HttpStatusCode.NoContent
            url.contains("/users/1") && method == HttpMethod.Patch -> HttpStatusCode.OK

            url.contains("/users/1") && method == HttpMethod.Put -> {
                if (request.headers["Authorization"] != null) {
                    HttpStatusCode.OK
                } else {
                    HttpStatusCode.Unauthorized
                }
            }
            url.contains("/users/1") && method == HttpMethod.Delete -> {
                if (request.headers["Authorization"] != null) {
                    HttpStatusCode.NoContent
                } else {
                    HttpStatusCode.OK
                }
            }
            url.contains("/users/999") && method == HttpMethod.Delete -> HttpStatusCode.NotFound

            url.contains("/timeout") -> HttpStatusCode.OK

            else -> HttpStatusCode.OK
        }
    }
}