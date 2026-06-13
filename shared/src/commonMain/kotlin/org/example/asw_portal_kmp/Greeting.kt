package org.example.asw_portal_kmp

import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.asw_portal_kmp.data.KeyValuePairManager
import org.example.asw_portal_kmp.data.createDataStore
import org.example.asw_portal_kmp.network.api.NetworkManager
import org.example.asw_portal_kmp.network.api.RequestOptions

class Greeting {
    private val platform = getPlatform()
    private val client = HttpClient(){
        defaultRequest {
            url("http://10.0.2.2:5100")
            contentType(ContentType.Application.Json)
        }
        expectSuccess = true
    }
    val store = createDataStore()
    private val kvManager = KeyValuePairManager(store)
    private val networkManager = NetworkManager(client, kvManager)
    suspend fun greet(): String {
        try {
            val response = networkManager.get<Int>(
                "/auth/login",
                emptyMap(),
                RequestOptions(),
                deserialize = { value ->
                    value.toInt()
                }
            )
            println(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return sayHello(platform.name)
    }
}