package org.example.asw_portal_kmp.camera

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class CameraManager(
    private val context: Context
)
{
    // Expose the preview use case so the UI layer can bind its surface provider
    val previewUseCase: Preview = Preview.Builder().build()
    private val imageCapture = ImageCapture.Builder().build()
    private var cameraProvider: ProcessCameraProvider? = null

    actual fun initialize(lifecycleOwner: Any) {
        val owner = lifecycleOwner as? LifecycleOwner ?: throw IllegalArgumentException("Requires LifecycleOwner")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider?.unbindAll()
            // Bind using the class-level previewUseCase
            cameraProvider?.bindToLifecycle(owner, cameraSelector, previewUseCase, imageCapture)
        }, ContextCompat.getMainExecutor(context))
    }

    actual fun startPreview() {
    }

    actual fun stopPreview() {
        cameraProvider?.unbindAll()
    }

    actual fun release() {
        cameraProvider?.unbindAll()
        cameraProvider = null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    actual suspend fun captureAndSaveImage(): Result<String> = runCatching {
        suspendCancellableCoroutine { continuation ->
            // Create a file in app-internal cache directory
            val outputDirectory = context.cacheDir
            val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
            val photoFile = File(outputDirectory, name)

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                context.mainExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        continuation.resume(photoFile.absolutePath)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    }
}