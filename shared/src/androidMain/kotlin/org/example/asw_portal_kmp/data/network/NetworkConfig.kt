package org.example.asw_portal_kmp.data.network

import android.content.pm.ApplicationInfo
import android.os.Build
import org.example.asw_portal_kmp.AndroidApplication

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class NetworkConfig {
    actual fun getBaseUrl(): String {
        return when {
            isDebug() -> getDebugBaseUrl()
            else -> "https://api.yourproduction.com"  // Production URL
        }
    }

    actual fun isDebug(): Boolean {
        return (AndroidApplication.getApplication().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun getDebugBaseUrl(): String {
        return when {
            isEmulator() -> "http://10.0.2.2:5100"
            else -> "http://10.0.0.124:5100"  // Your computer's IP for physical device
        }
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("google/sdk_gphone_") ||
                Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                Build.PRODUCT == "sdk" ||
                Build.PRODUCT == "sdk_x86" ||
                Build.PRODUCT == "sdk_google_phone_x86" ||
                Build.PRODUCT == "google_sdk" ||
                Build.PRODUCT == "emulator" ||
                Build.PRODUCT == "simulator" ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu") ||
                Build.HOST.startsWith("build") ||
                Build.HOST.contains("emulator"))
    }
}