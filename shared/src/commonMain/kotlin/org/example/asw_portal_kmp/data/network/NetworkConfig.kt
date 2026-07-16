package org.example.asw_portal_kmp.data.network

expect class NetworkConfig() {
    fun getBaseUrl(): String
    fun isDebug(): Boolean
}