package org.example.asw_portal_kmp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


interface KeyValuePairManager {
    suspend fun getIdToken(): String?
    suspend fun getTenantId(): String?
    suspend fun saveIdToken(token: String)
    suspend fun saveTenantId(tenantId: String)
}

class KeyValuePairManagerImplementation(
    private val store: DataStore<Preferences>
): KeyValuePairManager {

    companion object {
        // Preference keys
        private val KEY_ID_TOKEN = stringPreferencesKey("id_token")
        private val KEY_TENANT_ID = stringPreferencesKey("tenant_id")

        // Header names
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_TENANT_ID = "X-Tenant-Id"
        private const val TOKEN_PREFIX = "Bearer "
    }

    override suspend fun getIdToken(): String? {
        return store.data.map { preferences ->
            preferences[KEY_ID_TOKEN]
        }.first()
    }

    override suspend fun getTenantId(): String? {
        return store.data.map { preferences ->
            preferences[KEY_TENANT_ID]
        }.first()
    }

    override suspend fun saveIdToken(token: String) {
        store.edit { preferences ->
            preferences[KEY_ID_TOKEN] = token
        }
    }

    override suspend fun saveTenantId(tenantId: String) {
        store.edit { preferences ->
            preferences[KEY_TENANT_ID] = tenantId
        }
    }
}