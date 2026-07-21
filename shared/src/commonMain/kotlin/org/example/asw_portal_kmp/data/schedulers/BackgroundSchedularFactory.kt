package org.example.asw_portal_kmp.data.schedulers

expect class BackgroundSchedularFactory() {

    fun createScheduler(): BackgroundScheduler
}