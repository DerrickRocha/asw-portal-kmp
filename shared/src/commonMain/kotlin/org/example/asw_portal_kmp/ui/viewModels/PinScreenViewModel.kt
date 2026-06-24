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
import org.example.asw_portal_kmp.network.api.auth.ConfirmResult

class PinScreenViewModel(
    private val email: String,
    private val repository: AuthRepository = Dependencies.authRepository,
    private val dispatcher: CoroutineDispatcher = ioDispatcher()
): ViewModel() {

    private var verifyJob: Job? = null

    private val _state = MutableStateFlow(PinScreenState(email = email))
    val state: StateFlow<PinScreenState> = _state.asStateFlow()

    fun onPinDigitEntered(digit: Int) {
        if (_state.value.pin.length < 6 && !_state.value.isLoading && !_state.value.isVerified) {
            _state.value = _state.value.copy(
                pin = _state.value.pin + digit.toString(),
                error = null
            )
        }
    }

    fun onPinBackspace() {
        if (_state.value.pin.isNotEmpty() && !_state.value.isLoading && !_state.value.isVerified) {
            _state.value = _state.value.copy(
                pin = _state.value.pin.dropLast(1),
                error = null
            )
        }
    }

    fun onPinClear() {
        if (_state.value.pin.isNotEmpty() && !_state.value.isLoading && !_state.value.isVerified) {
            _state.value = _state.value.copy(
                pin = "",
                error = null
            )
        }
    }

    fun onConfirmPin() {
        if (_state.value.pin.length != 6 || _state.value.isLoading || _state.value.isVerified) {
            return
        }

        verifyJob?.cancel()
        verifyJob = viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val result = repository.confirm(_state.value.email, _state.value.pin)

                when (result) {
                    is ConfirmResult.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isVerified = true,
                            error = null
                        )
                    }
                    is ConfirmResult.Failure -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            pin = "", // Clear PIN on failure
                            error = result.error
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    pin = "",
                    error = e.message ?: "An error occurred during verification"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        verifyJob?.cancel()
    }

}

data class PinScreenState(
    val pin: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val isResending: Boolean = false,
    val error: String? = null
)

// Events

// Result Types


sealed class ResendPinResult {
    object Success : ResendPinResult()
    data class Failure(val error: String) : ResendPinResult()
}