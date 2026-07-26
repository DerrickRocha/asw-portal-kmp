package org.example.asw_portal_kmp.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import org.example.asw_portal_kmp.AndroidApplication

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = AndroidApplication.getApplication()
    val dbFile = appContext.getDatabasePath("asw_portal_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}