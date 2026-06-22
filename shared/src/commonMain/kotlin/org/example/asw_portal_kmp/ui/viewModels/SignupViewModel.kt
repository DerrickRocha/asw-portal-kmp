package org.example.asw_portal_kmp.ui.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignupViewModel: ViewModel() {

    private val _state = MutableStateFlow(SignupScreenState())
    val state: StateFlow<SignupScreenState> = _state.asStateFlow()


    fun onSignup() {

    }
}

data class SignupScreenState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val companyName: String = "",
    val isLoading: Boolean = false,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val companyNameError: String? = null,
    val generalError: String? = null
)