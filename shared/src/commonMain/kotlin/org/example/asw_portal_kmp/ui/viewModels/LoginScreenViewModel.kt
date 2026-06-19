package org.example.asw_portal_kmp.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.Dependencies
import org.example.asw_portal_kmp.network.api.auth.AuthRepository
import org.example.asw_portal_kmp.network.api.auth.LoginResult

class LoginScreenViewModel(private val repository: AuthRepository = Dependencies.authRepository) : ViewModel() {

    private var loginJob: Job? = null
    private val _state = MutableStateFlow(LoginScreenState())
    val state: StateFlow<LoginScreenState> = _state.asStateFlow()

    fun updateUsername(username: String) {
        _state.value = _state.value.copy(username = username, error = null, isSuccess = false)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password, error = null, isSuccess = false)
    }

    fun login() {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            val currentState = _state.value

            if (currentState.username.isBlank() || currentState.password.isBlank()) {
                _state.value = currentState.copy(
                    error = "Username and password are required",
                    isLoading = false
                )
                return@launch
            }

            _state.value = currentState.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )
            try {
                when (val result = repository.login(currentState.username, currentState.password)) {
                    is LoginResult.Failure -> _state.value = _state.value.copy(isLoading = false, error = result.error)
                    LoginResult.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            error = null
                        )
                        // Navigation handled by AppViewModel
                        // via KeyValuePairManager state changes
                    }
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Unknown error", isLoading = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        loginJob?.cancel()
    }

}

data class LoginScreenState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)