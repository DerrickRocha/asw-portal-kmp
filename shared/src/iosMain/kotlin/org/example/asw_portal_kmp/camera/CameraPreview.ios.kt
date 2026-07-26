package org.example.asw_portal_kmp.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(
    modifier: Modifier,
    cameraManager: CameraManager
) {
    UIKitView(factory = {
        val containerView = UIView()

        // Create the video preview layer bound to the session
        val previewLayer = AVCaptureVideoPreviewLayer.layerWithSession(cameraManager.captureSession)
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill

        // Layout handling: Adjust layer frame when the parent container shifts
        containerView.layer.addSublayer(previewLayer)

        // Keep the bounds synchronized with the view hierarchy
        containerView.autoresizesSubviews = true

        // Return the native view container
        containerView
    },
        modifier = modifier,
        update = { view ->
            // Update the layer frame to match the current view constraints
            val layer = view.layer.sublayers?.firstOrNull() as? AVCaptureVideoPreviewLayer
            layer?.frame = view.bounds
        },
        onRelease = {
            cameraManager.release()
        },
        properties = UIKitInteropProperties(isInteractive = true, isNativeAccessibilityEnabled = true))
}