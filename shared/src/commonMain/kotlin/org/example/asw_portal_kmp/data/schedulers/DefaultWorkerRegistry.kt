package org.example.asw_portal_kmp.data.schedulers

class DefaultWorkerRegistry: WorkerRegistry {
    private val workers = mutableMapOf<String, BackgroundWorker>()
    override fun registerWorker(
        name: String,
        worker: BackgroundWorker
    ) {
        workers[name] = worker
    }

    override fun getWorker(name: String): BackgroundWorker? {
        return workers[name]
    }
}