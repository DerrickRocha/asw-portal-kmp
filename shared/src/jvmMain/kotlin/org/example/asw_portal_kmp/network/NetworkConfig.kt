package org.example.asw_portal_kmp.network

import java.lang.management.ManagementFactory

actual class NetworkConfig {

    actual fun getBaseUrl(): String {
        return if (isDebug()) {
            "http://127.0.0.1:5100"  // Development
        } else {
            "https://agilesouthwest.com"  // Production
        }
    }

    actual fun isDebug(): Boolean {
        // Check JVM debug flags
        return ManagementFactory.getRuntimeMXBean().inputArguments
            .any { it.contains("-agentlib:jdwp") } ||  // Debugger attached
                System.getProperty("debug", "false").toBoolean()
    }
}