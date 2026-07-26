package org.example.asw_portal_kmp

import android.app.Application
import org.example.asw_portal_kmp.data.initDataStore

class ASWApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initDataStore(applicationContext)
        Dependencies.setupBackgroundTasks()
    }

}