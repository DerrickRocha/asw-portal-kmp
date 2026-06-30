package org.example.asw_portal_kmp.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.utils.io.ioDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.data.KeyValuePairManager

class AppViewModel(private val keyValuePairManager: KeyValuePairManager) : ViewModel() {

    private val _effects = MutableSharedFlow<AppEffects>()
    val effects: SharedFlow<AppEffects> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            keyValuePairManager.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    _effects.emit(AppEffects.NavigateToTenantSelection)
                } else {
                    _effects.emit(AppEffects.NavigateToLogin)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(ioDispatcher()) {
            keyValuePairManager.clear()
        }
    }
}

sealed interface AppEffects {
    data object NavigateToLogin : AppEffects
    data object NavigateToTenantSelection : AppEffects
}