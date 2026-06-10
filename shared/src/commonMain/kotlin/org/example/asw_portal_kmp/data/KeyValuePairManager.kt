package org.example.asw_portal_kmp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class KeyValuePairManager(
    private val store: DataStore<Preferences>
) {

    companion object {
        // Preference keys
        private val KEY_ID_TOKEN = stringPreferencesKey("id_token")
        private val KEY_TENANT_ID = stringPreferencesKey("tenant_id")

        // Header names
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_TENANT_ID = "X-Tenant-Id"
        private const val TOKEN_PREFIX = "Bearer "
    }

    suspend fun getIdToken(): String? {
        return store.data.map { preferences ->
            preferences[KEY_ID_TOKEN]
        }.first()
    }

    suspend fun getTenantId(): String? {
        return store.data.map { preferences ->
            preferences[KEY_TENANT_ID]
        }.first()
    }

    suspend fun saveIdToken(token: String) {
        store.edit { preferences ->
            preferences[KEY_ID_TOKEN] = token
        }
    }

    suspend fun saveTenantId(tenantId: String) {
        store.edit { preferences ->
            preferences[KEY_TENANT_ID] = tenantId
        }
    }
}