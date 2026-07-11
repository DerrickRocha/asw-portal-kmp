package org.example.asw_portal_kmp.data.database.tenants

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {

    @Insert
    suspend fun insert(tenant: TenantEntity)

    @Insert
    suspend fun insertAll(tenants: List<TenantEntity>)

    @Query("SELECT count(*) FROM tenants")
    suspend fun count(): Int

    @Query("SELECT * FROM tenants")
    fun getAllAsFlow(): Flow<List<TenantEntity>>

    @Query("SELECT * FROM tenants")
    fun getAll(): List<TenantEntity>

    @Query("SELECT MAX(updatedAt) FROM tenants")
    fun getLastModified(): Long
}