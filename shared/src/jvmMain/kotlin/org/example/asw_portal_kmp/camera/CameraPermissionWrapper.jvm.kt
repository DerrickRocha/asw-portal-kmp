package org.example.asw_portal_kmp.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CameraPermissionWrapper(
    modifier: Modifier,
    onPermissionDenied: @Composable () -> Unit,
    onPermissionGranted: @Composable () -> Unit
) {

}