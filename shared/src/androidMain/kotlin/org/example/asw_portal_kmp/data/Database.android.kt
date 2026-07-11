package org.example.asw_portal_kmp.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.example.asw_portal_kmp.data.database.AppDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("asw_portal_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}