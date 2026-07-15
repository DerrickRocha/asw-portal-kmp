package org.example.asw_portal_kmp.data.workers

sealed class KspWorkerType {
    object Tenants : KspWorkerType()
}

expect class KspWorker(){

    suspend fun doWork(type: KspWorkerType)
}