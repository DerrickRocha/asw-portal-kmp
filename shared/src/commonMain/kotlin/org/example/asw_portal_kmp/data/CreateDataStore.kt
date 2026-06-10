package org.example.asw_portal_kmp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createDataStore(): DataStore<Preferences>

internal const val dataStoreFileName = "dice.preferences_pb"