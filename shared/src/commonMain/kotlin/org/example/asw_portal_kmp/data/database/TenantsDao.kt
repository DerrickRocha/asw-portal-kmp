package org.example.asw_portal_kmp.data.database

import androidx.room.Dao
import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantsDao {
    @Insert
    suspend fun insert(tenant: TenantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tenants: List<TenantEntity>)

    @Query("SELECT count(*) FROM tenants")
    suspend fun count(): Int

    @Query("SELECT * FROM tenants")
    fun getAllAsFlow(): Flow<List<TenantEntity>>

    @Query("SELECT * FROM tenants")
    suspend fun getAll(): List<TenantEntity>

    @Query("SELECT MAX(updatedAt) FROM tenants")
    suspend fun getLastModified(): Long

}