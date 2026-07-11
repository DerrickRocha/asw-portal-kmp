package org.example.asw_portal_kmp.data.database

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RenameTable
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.example.asw_portal_kmp.data.database.tenants.TenantDao
import org.example.asw_portal_kmp.data.database.tenants.TenantEntity

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

@Database(
    entities = [TenantEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = AppDatabase.TenantRenameMigration::class)
    ]
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getTenantDao(): TenantDao
    @RenameTable(fromTableName = "TenantEntity", toTableName = "tenants")
    class TenantRenameMigration : AutoMigrationSpec
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
