package org.example.asw_portal_kmp.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.Dependencies

@Composable
fun CameraScreen(
    cameraManager: CameraManager = Dependencies.cameraManager,
    onImageCaptured: (String) -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var isNavigatingAway by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isNavigatingAway) {
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
                                isNavigatingAway = true
                                cameraManager.stopPreview()

                                onImageCaptured(filePath)
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
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        }

    }
}

@Composable
@Preview
fun CameraScreenPreview() {
    CameraScreen()
}