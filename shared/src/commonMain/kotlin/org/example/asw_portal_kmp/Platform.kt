package org.example.asw_portal_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform