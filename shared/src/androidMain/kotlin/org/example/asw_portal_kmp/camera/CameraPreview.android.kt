package org.example.asw_portal_kmp.camera

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    cameraManager: CameraManager
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PreviewView(context).apply {
                // Connect the native view to the CameraX pipeline
                cameraManager.previewUseCase.setSurfaceProvider(this.surfaceProvider)
            }
        },
        onRelease = {
            cameraManager.release()
        }
    )
}
