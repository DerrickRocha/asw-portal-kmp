package org.example.asw_portal_kmp.data.schedulers

interface WorkerRegistry {
    fun registerWorker(name: String, worker: BackgroundWorker)
    fun getWorker(name: String): BackgroundWorker?
}