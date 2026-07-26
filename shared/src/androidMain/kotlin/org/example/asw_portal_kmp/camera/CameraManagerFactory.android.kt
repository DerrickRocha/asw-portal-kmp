package org.example.asw_portal_kmp.camera

import org.example.asw_portal_kmp.AndroidApplication


actual fun createCameraManager(): CameraManager = CameraManager(AndroidApplication.getApplication())