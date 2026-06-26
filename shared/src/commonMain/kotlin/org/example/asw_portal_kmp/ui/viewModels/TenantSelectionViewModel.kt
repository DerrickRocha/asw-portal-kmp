package org.example.asw_portal_kmp.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.data.KeyValuePairManager
import org.example.asw_portal_kmp.network.api.tenants.TenantsRepository

class TenantSelectionViewModel(
    private val keyValuePairManager: KeyValuePairManager,
    private val repository: TenantsRepository
): ViewModel() {

    private val _state = MutableStateFlow(TenantSelectionState())
    val state: StateFlow<TenantSelectionState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TenantSelectionEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    init {
        loadTenants()
    }

    fun loadTenants() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val result = repository.getTenants()
                when (result.isSuccess) {
                    true -> _state.value = _state.value.copy(
                        tenants = result.getOrNull() ?: emptyList(),
                        isLoading = false
                    )
                    false -> _state.value = _state.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Failed to load tenants",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "An error occurred",
                    isLoading = false
                )
            }
        }
    }

    fun selectTenant(tenant: Tenant) {
        viewModelScope.launch {
            keyValuePairManager.saveTenantId(tenant.id)
            _events.emit(TenantSelectionEvent.NavigateToTenantConsole(tenant.id))
        }
    }
}

data class Tenant(val id: Int, val name: String, val domain: String, val customDomain: String, val updatedAt: String)
data class TenantSelectionState(
    val tenants: List<Tenant> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class TenantSelectionEvent {
    data class NavigateToTenantConsole(val tenantId: Int) : TenantSelectionEvent()
    object NavigateToCreateTenant : TenantSelectionEvent()
}