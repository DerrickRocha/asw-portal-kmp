package org.example.asw_portal_kmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "asw-portal-kmp",
    ) {
        App()
    }
}