package org.example.asw_portal_kmp.data.schedulers

import org.example.asw_portal_kmp.data.AndroidPlatform

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class BackgroundSchedularFactory {
    actual fun createScheduler(): BackgroundScheduler {
        return AndroidBackgroundScheduler(AndroidPlatform.applicationContext)
    }
}