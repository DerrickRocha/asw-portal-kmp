package org.example.asw_portal_kmp.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.utils.io.ioDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.Dependencies
import org.example.asw_portal_kmp.network.api.auth.AuthRepository

class SignupViewModel(
    private val repository: AuthRepository = Dependencies.authRepository,
    private val dispatcher: CoroutineDispatcher = ioDispatcher()
) : ViewModel() {


    private var signUpJob: Job? = null
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

    fun updateCompanyName(companyName: String) {
        _state.value = _state.value.copy(
            companyName = companyName,
            companyNameError = null,
            generalError = null
        )
    }

    fun signUp() {
        signUpJob?.cancel()
        signUpJob = viewModelScope.launch(dispatcher) {
            val currentState = _state.value

            // Validate all fields
            val validationResult = validateSignUp(currentState)
            if (!validationResult.isValid) {
                _state.value = currentState.copy(
                    emailError = validationResult.emailError,
                    passwordError = validationResult.passwordError,
                    confirmPasswordError = validationResult.confirmPasswordError,
                    subdomainError = validationResult.subdomainError,
                    customDomainError = validationResult.customDomainError,
                    generalError = validationResult.generalError,
                    isLoading = false
                )
                return@launch
            }

            // Start loading
            _state.value = currentState.copy(
                isLoading = true,
                generalError = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                subdomainError = null,
                customDomainError = null
            )

            try {
                val result = repository.signup(
                    email = currentState.email,
                    password = currentState.password,
                    subdomain = currentState.subdomain,
                    customDomain = currentState.customDomain.takeIf { it.isNotBlank() },
                    companyName = currentState.companyName
                )

                when (result) {
                    is SignupResult.Success -> {
                        _state.value = _state.value.copy(isLoading = false)
                        _events.emit(SignupEvent.NavigateToPinScreen(_state.value.email))
                    }

                    is SignupResult.Failure -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            generalError = result.error
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    generalError = e.message ?: "An error occurred during signup"
                )
            }
        }
    }

    private fun validateSignUp(state: SignupScreenState): ValidationResult {
        var isValid = true
        var emailError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null
        var subdomainError: String? = null
        var customDomainError: String? = null
        var generalError: String? = null

        // Validate Email
        if (state.email.isBlank()) {
            emailError = "Email is required"
            isValid = false
        } else if (!isValidEmail(state.email)) {
            emailError = "Please enter a valid email address"
            isValid = false
        }

        // Validate Password
        if (state.password.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        } else if (state.password.length < 8) {
            passwordError = "Password must be at least 8 characters"
            isValid = false
        } else if (!containsDigit(state.password)) {
            passwordError = "Password must contain at least one digit"
            isValid = false
        } else if (!containsUpperCase(state.password)) {
            passwordError = "Password must contain at least one uppercase letter"
            isValid = false
        } else if (!containsLowerCase(state.password)) {
            passwordError = "Password must contain at least one lowercase letter"
            isValid = false
        }

        // Validate Confirm Password
        if (state.confirmPassword.isBlank()) {
            confirmPasswordError = "Please confirm your password"
            isValid = false
        } else if (state.password != state.confirmPassword) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        }

        // Validate Subdomain
        if (state.subdomain.isBlank()) {
            subdomainError = "Subdomain is required"
            isValid = false
        } else if (state.subdomain.length < 3) {
            subdomainError = "Subdomain must be at least 3 characters"
            isValid = false
        } else if (state.subdomain.length > 63) {
            subdomainError = "Subdomain must be less than 63 characters"
            isValid = false
        } else if (!isValidSubdomain(state.subdomain)) {
            subdomainError = "Subdomain can only contain letters, numbers, and hyphens"
            isValid = false
        }

        // Validate Custom Domain (if provided)
        if (state.customDomain.isNotBlank()) {
            if (!isValidDomain(state.customDomain)) {
                customDomainError = "Please enter a valid domain name (e.g., portal.yourcompany.com)"
                isValid = false
            }
        }

        return ValidationResult(
            isValid = isValid,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            subdomainError = subdomainError,
            customDomainError = customDomainError,
            generalError = generalError
        )
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }

    private fun isValidSubdomain(subdomain: String): Boolean {
        // Subdomain can only contain letters, numbers, and hyphens
        // Cannot start or end with a hyphen
        val subdomainRegex = Regex("^[a-z0-9]([a-z0-9-]*[a-z0-9])?$")
        return subdomainRegex.matches(subdomain)
    }

    private fun isValidDomain(domain: String): Boolean {
        // Basic domain validation
        val domainRegex = Regex("^[a-zA-Z0-9]([a-zA-Z0-9-]*(\\.[a-zA-Z0-9-]+)*)\\.[a-zA-Z]{2,}$")
        return domainRegex.matches(domain)
    }

    private fun containsDigit(str: String): Boolean {
        return str.any { it.isDigit() }
    }

    private fun containsUpperCase(str: String): Boolean {
        return str.any { it.isUpperCase() }
    }

    private fun containsLowerCase(str: String): Boolean {
        return str.any { it.isLowerCase() }
    }

    override fun onCleared() {
        super.onCleared()
        signUpJob?.cancel()
    }

    private data class ValidationResult(
        val isValid: Boolean,
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,
        val subdomainError: String? = null,
        val customDomainError: String? = null,
        val generalError: String? = null
    )
}

// Updated State
data class SignupScreenState(
    val companyName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val subdomain: String = "",
    val customDomain: String = "",
    val isLoading: Boolean = false,
    val companyNameError: String? = null,
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
    data class NavigateToPinScreen(val email: String) : SignupEvent()
}

// Updated Result
sealed class SignupResult {
    data class Success(val tenantId: Int) : SignupResult()
    data class Failure(val error: String) : SignupResult()
}
