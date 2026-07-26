package org.example.asw_portal_kmp.camera

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidImageLoader() : ImageLoader {
    override suspend fun loadBitmapFromPath(filePath: String): ImageBitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = BitmapFactory.decodeFile(filePath)
            bitmap?.asImageBitmap()
        }.getOrNull()
    }
}