package org.example.asw_portal_kmp.data.schedulers

data class BackgroundConstraints(
    val requiresNetwork: Boolean = false,
    val requiresCharging: Boolean = false,
    val requiresDeviceIdle: Boolean = false,
    val requiresStorageNotLow: Boolean = false,
    val requiresUnmeteredNetwork: Boolean = false
)