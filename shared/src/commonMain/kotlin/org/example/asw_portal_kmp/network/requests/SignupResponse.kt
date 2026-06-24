package org.example.asw_portal_kmp.network.requests

data class SignupResponse(val tenantId: Int, val userId: Int, val requiresEmailVerification: Boolean)
