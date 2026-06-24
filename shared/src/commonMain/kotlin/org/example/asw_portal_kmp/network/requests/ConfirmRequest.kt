package org.example.asw_portal_kmp.network.requests

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmRequest (val email: String, val confirmationCode: String)