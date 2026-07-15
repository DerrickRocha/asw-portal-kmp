package org.example.asw_portal_kmp

import androidx.work.Configuration
import androidx.work.WorkManager
import org.example.asw_portal_kmp.data.AndroidPlatform
import org.example.asw_portal_kmp.data.workers.TenantWorkerFactory

object AndroidDependencies {
    val factory = TenantWorkerFactory(Dependencies.tenantsRepository)
    val configuration = Configuration.Builder()
        .setWorkerFactory(factory)
        .build()

    fun initWorkManager() = WorkManager.initialize(AndroidPlatform.applicationContext, configuration)
}
