package org.example.asw_portal_kmp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toOkioPath
import org.example.asw_portal_kmp.AndroidApplication

actual fun createDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { AndroidApplication.getApplication().filesDir.resolve(dataStoreFileName).toOkioPath() }
    )

