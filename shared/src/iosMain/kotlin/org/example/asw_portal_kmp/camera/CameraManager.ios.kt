package org.example.asw_portal_kmp.camera

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.fileDataRepresentation
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.writeToURL
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class CameraManager(
    private val photoOutput: AVCapturePhotoOutput
) {
    val captureSession = AVCaptureSession()
    @OptIn(ExperimentalForeignApi::class)
    actual fun initialize(lifecycleOwner: Any) {
        // iOS manages lifecycles via UIViewController or custom hosting layers
        val captureDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            ?: throw IllegalStateException("No camera available")

        val input = AVCaptureDeviceInput.deviceInputWithDevice(captureDevice, null) as AVCaptureDeviceInput

        if (captureSession.canAddInput(input)) {
            captureSession.addInput(input)
        }
    }

    actual fun startPreview() {
        if (!captureSession.isRunning()) {
            // Note: block-free dispatch might be required for production apps
            captureSession.startRunning()
        }
    }

    actual fun stopPreview() {
        if (captureSession.isRunning()) {
            captureSession.stopRunning()
        }
    }

    actual fun release() {
        stopPreview()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun captureAndSaveImage(): Result<String> = runCatching {
        suspendCancellableCoroutine { continuation ->
            val settings = AVCapturePhotoSettings.photoSettings()

            val photoCaptureDelegate = object : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
                override fun captureOutput(
                    output: AVCapturePhotoOutput,
                    didFinishProcessingPhoto: AVCapturePhoto,
                    error: NSError?
                ) {
                    if (error != null) {
                        continuation.resumeWithException(Exception(error.localizedDescription))
                        return
                    }

                    // Extract binary JPG image payload
                    val imageData = didFinishProcessingPhoto.fileDataRepresentation()
                    if (imageData == null) {
                        continuation.resumeWithException(Exception("Failed to decode image data"))
                        return
                    }

                    // Save to iOS Document directory safely
                    val fileManager = NSFileManager.defaultManager
                    val documentDirectory = fileManager.URLForDirectory(
                        NSDocumentDirectory,
                        NSUserDomainMask,
                        null,
                        true,
                        null
                    )

                    val filename = "${NSUUID.UUID().UUIDString}.jpg"
                    val fileURL = documentDirectory?.URLByAppendingPathComponent(filename)

                    if (fileURL != null && imageData.writeToURL(fileURL, true)) {
                        continuation.resume(fileURL.path ?: "")
                    } else {
                        continuation.resumeWithException(Exception("Failed writing photo to storage"))
                    }
                }
            }

            // Execute the hardware capture block
            photoOutput.capturePhotoWithSettings(settings, delegate = photoCaptureDelegate)
        }
    }
}