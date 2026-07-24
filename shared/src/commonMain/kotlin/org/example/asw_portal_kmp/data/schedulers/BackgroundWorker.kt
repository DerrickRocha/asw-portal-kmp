package org.example.asw_portal_kmp.data.schedulers

interface BackgroundWorker {
    suspend fun doWork(params: String?): BackgroundResult
}