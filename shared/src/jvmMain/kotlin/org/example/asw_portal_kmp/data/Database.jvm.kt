package org.example.asw_portal_kmp.data

import androidx.room.Room
import androidx.room.RoomDatabase
import org.example.asw_portal_kmp.data.database.AppDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "asw_portal_room.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}
