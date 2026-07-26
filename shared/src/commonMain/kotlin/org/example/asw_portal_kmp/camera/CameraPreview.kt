package org.example.asw_portal_kmp.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraManager: CameraManager
)