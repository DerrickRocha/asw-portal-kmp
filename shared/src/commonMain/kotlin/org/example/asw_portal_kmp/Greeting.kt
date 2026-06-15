package org.example.asw_portal_kmp

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.asw_portal_kmp.data.KeyValuePairManagerImplementation
import org.example.asw_portal_kmp.data.createDataStore
import org.example.asw_portal_kmp.network.NetworkConfig
import org.example.asw_portal_kmp.network.NetworkManager

class Greeting {
    private val platform = getPlatform()
    private val networkConfig = NetworkConfig()
    private val client = HttpClient(){
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }
        defaultRequest {
            val baseUrl = networkConfig.getBaseUrl()
            println("Base URL: $baseUrl")
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
        expectSuccess = true
    }
    val store = createDataStore()
    private val kvManager = KeyValuePairManagerImplementation(store)
    private val networkManager = NetworkManager(client, kvManager)

    @Serializable
    data class LoginResponse(val accessToken: String, val idToken: String, val refreshToken: String)
    @Serializable
    data class LoginRequest(val email: String, val password: String)
    suspend fun greet(): String {
        try {
            val response = networkManager.postJson<LoginRequest, LoginResponse>(
                "/auth/login",
                LoginRequest("drocha616@gmail.com", "JonSnow_666"),
            )
            println(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return sayHello(platform.name)
    }
}