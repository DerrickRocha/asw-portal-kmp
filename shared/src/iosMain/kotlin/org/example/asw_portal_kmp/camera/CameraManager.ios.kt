package org.example.asw_portal_kmp.camera

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVMediaTypeVideo

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class CameraManager() {
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
}