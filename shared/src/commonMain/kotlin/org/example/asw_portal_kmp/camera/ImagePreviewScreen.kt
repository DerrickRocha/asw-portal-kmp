package org.example.asw_portal_kmp.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import org.example.asw_portal_kmp.Dependencies

@Composable
fun ImagePreviewScreen(
    filePath: String,
    imageLoader: ImageLoader = Dependencies.imageLoader,
    modifier: Modifier = Modifier
) {
    var imageBitmap by remember(filePath) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(filePath) { mutableStateOf(true) }

    LaunchedEffect(filePath) {
        isLoading = true
        imageBitmap = imageLoader.loadBitmapFromPath(filePath)
        isLoading = false
    }

    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            imageBitmap != null -> {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = "Captured image preview",
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Text("Failed to load image preview.")
            }
        }
    }
}