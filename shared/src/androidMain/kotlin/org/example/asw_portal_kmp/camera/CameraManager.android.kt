package org.example.asw_portal_kmp.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class CameraManager(private val context: Context) {
    // Expose the preview use case so the UI layer can bind its surface provider
    val previewUseCase: Preview = Preview.Builder().build()
    private var cameraProvider: ProcessCameraProvider? = null

    actual fun initialize(lifecycleOwner: Any) {
        val owner = lifecycleOwner as? LifecycleOwner ?: throw IllegalArgumentException("Requires LifecycleOwner")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider?.unbindAll()
            // Bind using the class-level previewUseCase
            cameraProvider?.bindToLifecycle(owner, cameraSelector, previewUseCase)
        }, ContextCompat.getMainExecutor(context))
    }

    actual fun startPreview() {
    }

    actual fun stopPreview() {
        cameraProvider?.unbindAll()
    }

    actual fun release() {
        cameraProvider = null
    }
}