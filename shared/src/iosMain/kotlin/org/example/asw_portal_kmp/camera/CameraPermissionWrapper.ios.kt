package org.example.asw_portal_kmp.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

@Composable
actual fun CameraPermissionWrapper(
    modifier: Modifier,
    onPermissionDenied: @Composable () -> Unit,
    onPermissionGranted: @Composable () -> Unit
) {
    var permissionStatus by remember {
        mutableStateOf(AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo))
    }

    // Create a Compose-safe scope bound to the main thread
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (permissionStatus == AVAuthorizationStatusNotDetermined) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                // Use the Coroutine scope to switch back to the main thread cleanly
                scope.launch {
                    permissionStatus = if (granted) {
                        AVAuthorizationStatusAuthorized
                    } else {
                        AVAuthorizationStatusDenied
                    }
                }
            }
        }
    }

    if (permissionStatus == AVAuthorizationStatusAuthorized) {
        onPermissionGranted()
    } else {
        onPermissionDenied()
    }
}

