package org.example.asw_portal_kmp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TenantsDao {
    @Insert
    suspend fun insert(tenant: TenantEntity)

    @Query("SELECT count(*) FROM tenants")
    suspend fun count(): Int

    @Query("SELECT * FROM tenants")
    suspend fun getAllAsFlow(): List<TenantEntity>
}