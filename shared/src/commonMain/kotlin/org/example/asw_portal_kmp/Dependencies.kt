package org.example.asw_portal_kmp

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.asw_portal_kmp.data.Encryptor
import org.example.asw_portal_kmp.data.KeyValuePairManager
import org.example.asw_portal_kmp.data.KeyValuePairManagerImplementation
import org.example.asw_portal_kmp.data.createDataStore
import org.example.asw_portal_kmp.network.NetworkConfig
import org.example.asw_portal_kmp.network.NetworkManager
import org.example.asw_portal_kmp.network.NetworkManagerImplementation
import org.example.asw_portal_kmp.network.api.auth.AuthRepositoryImpl

object Dependencies {

    private val networkConfig = NetworkConfig()
    private val client = HttpClient(){
        install(ContentNegotiation) {
            json(
                Json {
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
    private val store = createDataStore()
    private val encryptor = Encryptor()
    val kvManager: KeyValuePairManager = KeyValuePairManagerImplementation(store, encryptor)
    private val networkManager = NetworkManagerImplementation(client, kvManager)

    val authRepository = AuthRepositoryImpl(networkManager, kvManager)

}