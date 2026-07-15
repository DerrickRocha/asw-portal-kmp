package org.example.asw_portal_kmp.data.workers

actual class KspWorker {

    actual suspend fun doWork(type: KspWorkerType) {
        when(type){
            KspWorkerType.Tenants -> runTenantsWorker()
        }
    }

    private fun runTenantsWorker() {

    }
}