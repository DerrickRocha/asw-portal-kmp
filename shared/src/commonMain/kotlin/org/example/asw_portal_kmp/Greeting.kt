package org.example.asw_portal_kmp

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import org.example.asw_portal_kmp.data.KeyValuePairManager
import org.example.asw_portal_kmp.data.KeyValuePairManagerImplementation
import org.example.asw_portal_kmp.data.createDataStore
import org.example.asw_portal_kmp.network.NetworkManager
import org.example.asw_portal_kmp.network.RequestOptions

class Greeting {
    private val platform = getPlatform()
    private val client = HttpClient(){
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            url("http://10.0.2.2:5100")
            contentType(ContentType.Application.Json)
        }
        expectSuccess = true
    }
    val store = createDataStore()
    private val kvManager = KeyValuePairManagerImplementation(store)
    private val networkManager = NetworkManager(client, kvManager)

    @Serializable
    private data class LoginRequest(val email: String, val password: String)
    suspend fun greet(): String {
        try {
            val response = networkManager.post<LoginRequest, String>(
                "/auth/login",
                LoginRequest("drocha616@gmail.com", "JonSnow_666"),
                deserialize = { value ->
                    value
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return sayHello(platform.name)
    }
}