package org.example.asw_portal_kmp.ui.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.asw_portal_kmp.Dependencies
import org.example.asw_portal_kmp.network.api.auth.AuthRepository

class SignupViewModel(private val repository: AuthRepository = Dependencies.authRepository): ViewModel() {

    private val _state = MutableStateFlow(SignupScreenState())
    val state: StateFlow<SignupScreenState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SignupEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(
            email = email,
            emailError = null,
            generalError = null
        )
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(
            password = password,
            passwordError = null,
            generalError = null
        )
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _state.value = _state.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null,
            generalError = null
        )
    }

    fun updateSubdomain(subdomain: String) {
        // Convert to lowercase and remove spaces
        val sanitized = subdomain.lowercase().replace(" ", "")
        _state.value = _state.value.copy(
            subdomain = sanitized,
            subdomainError = null,
            generalError = null
        )
    }

    fun updateCustomDomain(customDomain: String) {
        // Convert to lowercase and remove spaces
        val sanitized = customDomain.lowercase().replace(" ", "")
        _state.value = _state.value.copy(
            customDomain = sanitized,
            customDomainError = null,
            generalError = null
        )
    }

    fun signUp() {

    }
}

// Updated State
data class SignupScreenState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val subdomain: String = "",
    val customDomain: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val subdomainError: String? = null,
    val customDomainError: String? = null,
    val generalError: String? = null
)

// Updated Event
sealed class SignupEvent {
    object NavigateToLogin : SignupEvent()
    data class NavigateToTenantConsole(val tenantId: Int) : SignupEvent()
}

// Updated Result
sealed class SignupResult {
    data class Success(val tenantId: Int) : SignupResult()
    data class Failure(val error: String) : SignupResult()
}
