package org.example.asw_portal_kmp.network.api.auth

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.example.asw_portal_kmp.data.KeyValuePairManager
import org.example.asw_portal_kmp.network.NetworkManager
import org.example.asw_portal_kmp.network.NetworkResult
import org.example.asw_portal_kmp.network.requests.LoginRequest
import org.example.asw_portal_kmp.network.responses.LoginResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRepositoryTest {
    private val mockKeyValueManager = mock<KeyValuePairManager>(mode = MockMode.autofill)
    private val mockNetworkManager = mock<NetworkManager>(mode = MockMode.autofill)

    private val testDispatcher = Dispatchers.Unconfined

    @Test
    fun testAuth_whenUsernameMissing_loginFailsWithFieldErrorMessage() = runTest {
        val username = ""
        val password = "xyz"
        val expectedErrorMessage = "Username and password are required"


        val repository = AuthRepositoryImpl(mockNetworkManager, mockKeyValueManager, testDispatcher)
        val result = repository.login(username, password)

        assertEquals(LoginResult.Failure(expectedErrorMessage), result)
    }

    @Test
    fun testAuth_whenPasswordMissing_loginFailsWithFieldErrorMessage() = runTest {
        val username = "abc"
        val password = ""
        val expectedErrorMessage = "Username and password are required"
        val repository = AuthRepositoryImpl(mockNetworkManager, mockKeyValueManager, testDispatcher)
        val result = repository.login(username, password)
        assertEquals(LoginResult.Failure(expectedErrorMessage), result)
    }

    @Test
    fun testAuth_whenUsernameAndPasswordValid_loginSucceeds() = runTest {
        val username = "abc"
        val password = "xyz"
        val accessToken = "valid-token"
        val idToken = "valid-id-token"
        val refreshToken = "valid-refresh-token"

        val loginRequest = LoginRequest(username, password)
        val loginResponse = LoginResponse(accessToken, idToken, refreshToken)

        everySuspend {
            mockNetworkManager.post(
                url = "/auth/login",
                requestBody = loginRequest,
                params = emptyMap(),
                options = any(),
                serialize = any(),
                deserialize = any<(String) -> LoginResponse>()
            )
        } .returns(NetworkResult.Success(loginResponse))

        everySuspend {
            mockKeyValueManager.saveIdToken(idToken)
        } .returns(Unit)



        val repository = AuthRepositoryImpl(mockNetworkManager, mockKeyValueManager, testDispatcher)
        val result = repository.login(username, password)

        assertTrue(result is LoginResult.Success)
        verifySuspend {
            mockKeyValueManager.saveIdToken(idToken)
        }
        verifySuspend {
            mockNetworkManager.post(
                url = "/auth/login",
                requestBody = loginRequest,
                params = emptyMap(),
                options = any(),
                serialize = any(),
                deserialize = any<(String) -> LoginResponse>()
            )
        }

    }
}