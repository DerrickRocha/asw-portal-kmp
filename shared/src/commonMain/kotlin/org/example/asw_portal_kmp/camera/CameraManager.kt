package org.example.asw_portal_kmp.camera

import androidx.compose.runtime.CompositionLocalProvider

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class CameraManager {

    suspend fun captureAndSaveImage(): Result<String> // Returns local file path on success
    fun initialize(lifecycleOwner: Any)
    fun startPreview()
    fun stopPreview()
    fun release()
}