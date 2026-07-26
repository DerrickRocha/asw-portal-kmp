package org.example.asw_portal_kmp.camera

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

class IosImageLoader : ImageLoader {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun loadBitmapFromPath(filePath: String): ImageBitmap? = withContext(Default) {
        runCatching {
            val nsData = NSData.dataWithContentsOfFile(filePath) ?: return@runCatching null
            // 2. Safely read NSData bytes into a Kotlin ByteArray
            val byteArray = ByteArray(nsData.length.toInt()).apply {
                usePinned { pinned ->
                    memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
                }
            }

            // 3. Convert bytes to a Skia Image and then a Compose ImageBitmap
            val skiaImage = Image.makeFromEncoded(byteArray)
            skiaImage.toComposeImageBitmap()
        }.getOrNull()
    }
}