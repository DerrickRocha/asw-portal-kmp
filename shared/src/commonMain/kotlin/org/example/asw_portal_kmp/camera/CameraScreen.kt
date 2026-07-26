package org.example.asw_portal_kmp.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.example.asw_portal_kmp.Dependencies

@Composable
fun CameraScreen(cameraManager: CameraManager = Dependencies.cameraManager) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPermissionWrapper(
            onPermissionDenied = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera permission is required to use this feature.")
                }
            },
            onPermissionGranted = {
                // Safe Zone: Hardware initialization only happens after authorization
                LaunchedEffect(lifecycleOwner) {
                    cameraManager.initialize(lifecycleOwner)
                    cameraManager.startPreview()
                }

                // Render the platform-native preview layers
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    cameraManager = cameraManager
                )
            }
        )
    }
}

@Composable
@Preview
fun CameraScreenPreview() {
    CameraScreen()
}