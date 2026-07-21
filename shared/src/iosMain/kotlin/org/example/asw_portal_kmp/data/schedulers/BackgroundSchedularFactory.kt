package org.example.asw_portal_kmp.data.schedulers

actual class BackgroundSchedularFactory actual constructor() {
    actual fun createScheduler(): BackgroundScheduler = IosBackgroundScheduler()
}