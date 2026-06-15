package org.example.asw_portal_kmp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toOkioPath

actual fun createDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { AndroidPlatform.applicationContext.filesDir.resolve(dataStoreFileName).toOkioPath() }
    )

fun initDataStore(context: Context) {
    AndroidPlatform.applicationContext = context.applicationContext
}

object AndroidPlatform {
    lateinit var applicationContext: Context
}