package org.example.asw_portal_kmp

import android.app.Application

class ASWApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidDependencies.initWorkManager()
    }
}