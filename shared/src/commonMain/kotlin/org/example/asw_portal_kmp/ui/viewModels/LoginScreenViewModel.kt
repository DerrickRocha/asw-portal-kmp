package org.example.asw_portal_kmp.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginScreenState())
    val state: StateFlow<LoginScreenState> = _state.asStateFlow()

    fun updateUsername(username: String) {
        _state.value = _state.value.copy(username = username)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun login() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

        }
    }

}

data class LoginScreenState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)