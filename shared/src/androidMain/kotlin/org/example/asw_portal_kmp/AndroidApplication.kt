package org.example.asw_portal_kmp

import android.app.Application

object AndroidApplication {
    private lateinit var application: Application
    fun getApplication() = application

    fun init(application: Application) {
        this.application = application
    }
}