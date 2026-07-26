package org.example.asw_portal_kmp.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraPermissionWrapper(
    modifier: Modifier = Modifier,
    onPermissionDenied: @Composable () -> Unit,
    onPermissionGranted: @Composable () -> Unit
)