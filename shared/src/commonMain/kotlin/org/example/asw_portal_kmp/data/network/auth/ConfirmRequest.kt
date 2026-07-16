package org.example.asw_portal_kmp.data.network.auth

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmRequest (val email: String, val confirmationCode: String)