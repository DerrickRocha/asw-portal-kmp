package org.example.asw_portal_kmp.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.FileStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import java.io.File

actual fun createDataStore(): DataStore<Preferences> = DataStoreFactory.create(
    storage = FileStorage(
        serializer = PreferencesSerializer,
        produceFile = { File(System.getProperty("java.io.tmpdir"), dataStoreFileName) }
    )
)