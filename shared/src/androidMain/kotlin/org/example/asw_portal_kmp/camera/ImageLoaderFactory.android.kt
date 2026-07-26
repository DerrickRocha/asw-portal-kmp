package org.example.asw_portal_kmp.camera

actual fun createImageLoader(): ImageLoader {
    return AndroidImageLoader()
}