package org.example.asw_portal_kmp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


interface KeyValuePairManager {

    val isLoggedIn: Flow<Boolean>
    suspend fun getIdToken(): String?
    suspend fun getTenantId(): Int?
    suspend fun saveIdToken(token: String)
    suspend fun saveTenantId(tenantId: Int)
}

class KeyValuePairManagerImplementation(
    private val store: DataStore<Preferences>,
    private val encryptor: Encryptor
): KeyValuePairManager {

    companion object {
        // Preference keys
        private val KEY_ID_TOKEN = stringPreferencesKey("id_token")
        private val KEY_TENANT_ID = intPreferencesKey("tenant_id")

        // Header names
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_TENANT_ID = "X-Tenant-Id"
        private const val TOKEN_PREFIX = "Bearer "
    }

    override val isLoggedIn: Flow<Boolean>
        get() = store.data.map { preferences ->
            preferences[KEY_ID_TOKEN].isNullOrBlank().not()
        }

    override suspend fun getIdToken(): String? {
        return store.data.map { preferences ->
            val token = preferences[KEY_ID_TOKEN]
            if (token.isNullOrBlank()) return@map null
            encryptor.decrypt(token)
        }.first()
    }

    override suspend fun getTenantId(): Int? {
        return store.data.map { preferences ->
            preferences[KEY_TENANT_ID]
        }.first()
    }

    override suspend fun saveIdToken(token: String) {
        store.edit { preferences ->
            val encryptedToken = encryptor.encrypt(token)
            preferences[KEY_ID_TOKEN] = encryptedToken
        }
    }

    override suspend fun saveTenantId(tenantId: Int) {
        store.edit { preferences ->
            preferences[KEY_TENANT_ID] = tenantId
        }
    }
}