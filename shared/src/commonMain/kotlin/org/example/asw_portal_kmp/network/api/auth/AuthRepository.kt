package org.example.asw_portal_kmp.network.api.auth

import org.example.asw_portal_kmp.data.KeyValuePairManager
import org.example.asw_portal_kmp.network.NetworkManager
import org.example.asw_portal_kmp.network.NetworkResult
import org.example.asw_portal_kmp.network.requests.LoginRequest
import org.example.asw_portal_kmp.network.responses.LoginResponse

interface AuthRepository {

    suspend fun login(username: String, password: String): LoginResult
}

class AuthRepositoryImpl(
    private val networkManager: NetworkManager,
    private val keyValuePairManager: KeyValuePairManager
) : AuthRepository {

    override suspend fun login(
        username: String,
        password: String
    ): LoginResult {
        val request = LoginRequest(username, password)
        val response = networkManager.postJson<LoginRequest, LoginResponse>(
            url = "/auth/login", requestBody = request
        )
        return when (response) {
            is NetworkResult.Error -> {
                LoginResult.Failure("Login failed: ${response.statusCode}")
            }
            is NetworkResult.Exception -> {
                LoginResult.Failure("Login failed. Please try again later.")
            }
            is NetworkResult.Success<LoginResponse> -> {
                keyValuePairManager.saveIdToken(response.data.idToken)
                LoginResult.Success
            }
        }
    }

}

