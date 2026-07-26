package org.example.asw_portal_kmp.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.Dependencies

@Composable
fun CameraScreen(cameraManager: CameraManager = Dependencies.cameraManager) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

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
        // Capture Floating Button overlay
        Button(
            onClick = {
                scope.launch {
                    cameraManager.captureAndSaveImage()
                        .onSuccess { filePath ->
                            println("Photo saved successfully at: $filePath")
                            // TODO: Pass path to your view model or preview container
                        }
                        .onFailure { error ->
                            println("Error capturing photo: ${error.message}")
                        }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            Text("Capture Photo")
        }
    }
}

@Composable
@Preview
fun CameraScreenPreview() {
    CameraScreen()
}