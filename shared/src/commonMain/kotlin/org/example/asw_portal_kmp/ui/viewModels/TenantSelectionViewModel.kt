package org.example.asw_portal_kmp.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.Dependencies
import org.example.asw_portal_kmp.data.AppConfiguration
import org.example.asw_portal_kmp.data.models.Tenant
import org.example.asw_portal_kmp.data.repositories.RepositoryResult
import org.example.asw_portal_kmp.data.repositories.tenants.TenantsRepository

class TenantSelectionViewModel(
    private val repository: TenantsRepository = Dependencies.tenantsRepository,
    private val configuration: AppConfiguration = Dependencies.appConfiguration,
) : ViewModel() {

    private val _state = MutableStateFlow(TenantSelectionState())
    val state: StateFlow<TenantSelectionState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TenantSelectionEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    init {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.tenants.collect { items ->
                _state.update { it.copy(networkTenants = items.toImmutableList(), isLoading = false) }
            }
        }
        viewModelScope.launch {
            repository.syncTenants()
        }
    }


    fun selectTenant(networkTenant: Tenant) {
        viewModelScope.launch {
            configuration.saveTenantId(networkTenant.id)
            _events.emit(TenantSelectionEvent.NavigateToTenantConsole(networkTenant.id))
        }
    }

    fun retry() {
      //  loadTenants()
    }

    fun deleteTenant(tenantId: Int) {
        viewModelScope.launch {
            _state.update {
                it.copy(isDeleting = true, deleteError = null)
            }
            try {
                when (val result = repository.deleteTenant(tenantId)) {
                    is RepositoryResult.Failure -> {
                        _state.update { it.copy(isDeleting = false, deleteError = result.message) }
                        _events.emit(TenantSelectionEvent.DeleteTenantError)
                    }

                    is RepositoryResult.Success<Unit> -> {
                        _state.update { currentState ->
                            currentState.copy(
                                networkTenants = currentState.networkTenants.filter { it.id != tenantId }.toImmutableList(),
                                isDeleting = false,
                                deleteError = null
                            )
                        }
                    }
                }
            } catch (exception: Exception) {
                _state.update {
                    it.copy(
                        isDeleting = false,
                        deleteError = exception.message ?: "Failed to delete tenant"
                    )
                }
            }

        }
    }
}

data class TenantSelectionState(
    val networkTenants: ImmutableList<Tenant> = persistentListOf(), // Guaranteed stable by Compose
    val isLoading: Boolean = false,
    val loadError: String? = null,        // Specific to loading
    val deleteError: String? = null,      // Specific to deletion
    val updateError: String? = null,      // Specific to updates
    val isDeleting: Boolean = false,      // Track delete operation
    val isUpdating: Boolean = false,      // Track update operation
    val selectedTenantId: Int? = null
)

sealed class TenantSelectionEvent {
    data class NavigateToTenantConsole(val tenantId: Int) : TenantSelectionEvent()
    data object DeleteTenantError : TenantSelectionEvent()
}