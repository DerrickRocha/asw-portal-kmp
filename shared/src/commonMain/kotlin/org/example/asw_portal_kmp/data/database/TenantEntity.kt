package org.example.asw_portal_kmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tenants")
data class TenantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val subDomain: String,
    val customDomain: String?,
    val createdAt: String,
    val updatedAt: String,
    val rowVersion: String,
    val isDone: Boolean,
    val lastModified: Long,
    val isSynced: Boolean = false
)