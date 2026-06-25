package org.example.asw_portal_kmp.network.api.auth

import io.ktor.utils.io.ioDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.example.asw_portal_kmp.data.KeyValuePairManager
import org.example.asw_portal_kmp.network.AuthenticationException
import org.example.asw_portal_kmp.network.JsonParsingException
import org.example.asw_portal_kmp.network.NetworkManager
import org.example.asw_portal_kmp.network.NetworkResult
import org.example.asw_portal_kmp.network.TenantException
import org.example.asw_portal_kmp.network.postJson
import org.example.asw_portal_kmp.network.requests.ConfirmRequest
import org.example.asw_portal_kmp.network.requests.LoginRequest
import org.example.asw_portal_kmp.network.requests.SignupRequest
import org.example.asw_portal_kmp.network.requests.SignupResponse
import org.example.asw_portal_kmp.network.responses.ConfirmResponse
import org.example.asw_portal_kmp.network.responses.LoginResponse
import org.example.asw_portal_kmp.ui.viewModels.SignupResult

interface AuthRepository {

    suspend fun login(username: String, password: String): LoginResult
    suspend fun signup(companyName: String, email: String, password: String, subdomain: String, customDomain: String?): SignupResult

    suspend fun confirm(email: String, confirmationCode: String): ConfirmResult
}

class AuthRepositoryImpl(
    private val networkManager: NetworkManager,
    private val keyValuePairManager: KeyValuePairManager,
    private val ioDispatcher: CoroutineDispatcher = ioDispatcher()
) : AuthRepository {

    override suspend fun login(
        username: String,
        password: String
    ): LoginResult {

        return withContext(ioDispatcher){
            if (username.isBlank() || password.isBlank()) {
                return@withContext LoginResult.Failure("Username and password are required")
            }
            val request = LoginRequest(username, password)
            val response = networkManager.postJson<LoginRequest, LoginResponse>(
                url = "/auth/login", requestBody = request
            )

            when (response) {
                is NetworkResult.Error -> {
                    val message = when (response.statusCode) {
                        401 -> "Invalid username or password"
                        403 -> "Account locked. Please contact support."
                        404 -> "Service unavailable. Please try again later."
                        else -> "Login failed (${response.statusCode})"
                    }
                    LoginResult.Failure(message)
                }
                is NetworkResult.Exception -> {
                    val message = when (val e = response.throwable) {
                        is JsonParsingException -> "Server response format error. Please contact support."
                        is AuthenticationException -> "Authentication error. Please try again."
                        is TenantException -> "Tenant configuration error. Please contact support."
                        is kotlinx.coroutines.TimeoutCancellationException -> "Request timed out. Please try again."
                        else -> "Login failed. Please try again later."
                    }
                    LoginResult.Failure(message)
                }
                is NetworkResult.Success<LoginResponse> -> {
                    keyValuePairManager.saveIdToken(response.data.idToken)
                    LoginResult.Success
                }
            }
        }
    }

    override suspend fun signup(
        companyName: String,
        email: String,
        password: String,
        subdomain: String,
        customDomain: String?
    ): SignupResult {
        return withContext(ioDispatcher) {
            val request = SignupRequest(companyName, email, password, subdomain, customDomain)
            val response = networkManager.postJson<SignupRequest, SignupResponse>("/auth/register", request)
            when (response) {
                is NetworkResult.Error -> SignupResult.Failure(response.message)
                is NetworkResult.Exception -> SignupResult.Failure(response.throwable.message ?: "Signup failed")
                is NetworkResult.Success<SignupResponse> -> {
                    SignupResult.Success(response.data.tenantId)
                }
            }
        }
    }

    override suspend fun confirm(
        email: String,
        confirmationCode: String
    ): ConfirmResult {
        return withContext(ioDispatcher) {
            val response = networkManager.postJson<ConfirmRequest, ConfirmResponse>("/auth/confirm", ConfirmRequest(email, confirmationCode))
            when (response) {
                is NetworkResult.Error -> ConfirmResult.Failure(response.message)
                is NetworkResult.Exception -> ConfirmResult.Failure(response.throwable.message ?: "Confirmation failed")
                is NetworkResult.Success<ConfirmResponse> -> ConfirmResult.Success
            }

        }
    }

}

