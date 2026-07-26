package org.example.asw_portal_kmp.camera

import androidx.compose.ui.graphics.ImageBitmap

interface ImageLoader {
    suspend fun loadBitmapFromPath(filePath: String): ImageBitmap?
}