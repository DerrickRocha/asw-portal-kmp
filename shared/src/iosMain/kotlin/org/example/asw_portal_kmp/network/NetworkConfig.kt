package org.example.asw_portal_kmp.network

import platform.Foundation.NSProcessInfo
import kotlin.experimental.ExperimentalNativeApi

actual class NetworkConfig {
    actual fun getBaseUrl(): String {
        return if (isDebug()) {
            getDebugBaseUrl()
        } else {
            "https://agilesouthwest.com"  // Production URL
        }
    }

    @OptIn(ExperimentalNativeApi::class)
    actual fun isDebug(): Boolean {
        // Check if running in debug mode
        return Platform.isDebugBinary
    }

    private fun getDebugBaseUrl(): String {
        return if (isSimulator()) {
            "http://127.0.0.1:5100"  // Use 127.0.0.1 or localhost for simulator
        } else {
            "http://192.168.1.100:5100"  // Your computer's IP for physical device
        }
    }

    private fun isSimulator(): Boolean {
        return NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null
    }
}