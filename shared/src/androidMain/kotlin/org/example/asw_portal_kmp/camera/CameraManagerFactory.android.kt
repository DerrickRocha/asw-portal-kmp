package org.example.asw_portal_kmp.camera

import org.example.asw_portal_kmp.data.AndroidPlatform


actual fun createCameraManager(): CameraManager = CameraManager(AndroidPlatform.applicationContext)