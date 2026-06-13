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
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString
import org.example.asw_portal_kmp.data.KeyValuePairManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkManagerTest {

    @Serializable
    data class TestUser(val id: Int, val name: String, val email: String)
    private lateinit var networkManager: NetworkManager
    private val mockKeyValueManager = mock<KeyValuePairManager>(mode = MockMode.autofill)
    private lateinit var mockClient: HttpClient

    @BeforeTest
    fun setup() {
        // Create a real MockEngine with proper handler
        val mockEngine = MockEngine { request ->
            respond(
                content = getMockResponseForUrl(request.url.toString()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        networkManager = NetworkManager(client, mockKeyValueManager)
    }

    @Test
    fun testGetWithAuthentication() = runTest {
        everySuspend { mockKeyValueManager.getIdToken() } returns "test-token-123"

        // When
        val result = networkManager.get(
            url = "https://api.example.com/users/1",
            options = RequestOptions(isAuthRequired = true),
            deserialize = {
                decodeFromString<TestUser>(it)
            }
        )

        // Then - Verify the mock was called
        verifySuspend { mockKeyValueManager.getIdToken() }

        // Assert result is success
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun testGetWithMissingAuthToken() = runTest {
        // Given - Mock returns null (no token)
        everySuspend { mockKeyValueManager.getIdToken() } returns null

        // When
        val result = networkManager.get<TestUser>(
            url = "https://api.example.com/users/1",
            options = RequestOptions(isAuthRequired = true),
            deserialize = { decodeFromString<TestUser>(it) }
        )

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals(result.message.contains("Authentication required"), true)
    }

    private fun getMockResponseForUrl(url: String): String {
        return when {
            url.contains("/users/1") -> """{"id":1,"name":"John","email":"john@example.com"}"""
            else -> "{}"
        }
    }
}