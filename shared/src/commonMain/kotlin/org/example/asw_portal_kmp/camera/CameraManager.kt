package org.example.asw_portal_kmp.camera

import androidx.compose.runtime.CompositionLocalProvider

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class CameraManager {
    fun initialize(lifecycleOwner: Any)
    fun startPreview()
    fun stopPreview()
    fun release()
}