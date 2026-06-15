package org.example.asw_portal_kmp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.io.File

actual fun createDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    produceFile = {
        // Use the user's AppData/Application Support directory for persistence
        val appDataDir = File(System.getProperty("user.home"), ".myapp-settings")
        appDataDir.mkdirs() // Ensure directory exists
        File(appDataDir, "app.preferences_pb").absolutePath.toPath()
    }
)