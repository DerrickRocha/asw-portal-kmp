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
import org.example.asw_portal_kmp.Dependencies
import org.example.asw_portal_kmp.data.KeyValuePairManager
import org.example.asw_portal_kmp.network.api.tenants.Result
import org.example.asw_portal_kmp.network.api.tenants.Tenant
import org.example.asw_portal_kmp.network.api.tenants.TenantsRepository

class TenantSelectionViewModel(
    private val keyValuePairManager: KeyValuePairManager = Dependencies.kvManager,
    private val repository: TenantsRepository = Dependencies.tenantsRepository
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
                when(result) {
                    is Result.Failure -> _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                    is Result.Success<List<Tenant>> -> _state.value = _state.value.copy(
                        tenants = result.data,
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

    fun retry() {
        loadTenants()
    }
}

data class TenantSelectionState(
    val tenants: List<Tenant> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class TenantSelectionEvent {
    data class NavigateToTenantConsole(val tenantId: Int) : TenantSelectionEvent()
}