package org.example.asw_portal_kmp

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ioDispatcher
import kotlinx.serialization.json.Json
import org.example.asw_portal_kmp.data.Encryptor
import org.example.asw_portal_kmp.data.KeyValuePairManager
import org.example.asw_portal_kmp.data.KeyValuePairManagerImplementation
import org.example.asw_portal_kmp.data.createDataStore
import org.example.asw_portal_kmp.data.database.getDatabaseBuilder
import org.example.asw_portal_kmp.data.database.getRoomDatabase
import org.example.asw_portal_kmp.data.network.NetworkConfig
import org.example.asw_portal_kmp.data.network.NetworkManagerImplementation
import org.example.asw_portal_kmp.data.repositories.auth.AuthRepository
import org.example.asw_portal_kmp.data.repositories.auth.AuthRepositoryImpl
import org.example.asw_portal_kmp.data.repositories.tenants.TenantsRepository
import org.example.asw_portal_kmp.data.repositories.tenants.TenantsRepositoryImplementation
import org.example.asw_portal_kmp.data.workers.KspWorker

object Dependencies {

    private val networkConfig = NetworkConfig()
    private val client = HttpClient() {
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

    val database = getRoomDatabase(getDatabaseBuilder())
    private val store = createDataStore()
    private val encryptor = Encryptor()
    val kvManager: KeyValuePairManager = KeyValuePairManagerImplementation(store, encryptor)
    private val networkManager = NetworkManagerImplementation(client, kvManager)

    private val dispatcher = ioDispatcher()
    val authRepository: AuthRepository = AuthRepositoryImpl(networkManager, kvManager, dispatcher)

    private val worker = KspWorker()
    val tenantsRepository: TenantsRepository = TenantsRepositoryImplementation(
        networkManager,
        dispatcher,
        database.getTenantDao(),
        worker
    )
}