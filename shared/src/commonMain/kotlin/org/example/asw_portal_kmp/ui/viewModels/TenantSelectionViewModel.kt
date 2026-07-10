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
import org.example.asw_portal_kmp.network.api.RepositoryResult
import org.example.asw_portal_kmp.network.api.tenants.Tenant
import org.example.asw_portal_kmp.network.api.tenants.TenantsRepository

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
        loadTenants()
    }

    fun loadTenants() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    loadError = null,
                    deleteError = null,
                )
            }

            try {
                when (val result = repository.getTenants()) {
                    is RepositoryResult.Failure -> _state.update {
                        it.copy(
                            loadError = result.message,
                            isLoading = false
                        )
                    }

                    is RepositoryResult.Success<List<Tenant>> -> _state.update {
                        it.copy(
                            tenants = result.data.toImmutableList(), // Safe conversion to stable type
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loadError = e.message ?: "An error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectTenant(tenant: Tenant) {
        viewModelScope.launch {
            configuration.saveTenantId(tenant.tenantId)
            _events.emit(TenantSelectionEvent.NavigateToTenantConsole(tenant.tenantId))
        }
    }

    fun retry() {
        loadTenants()
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
                                tenants = currentState.tenants.filter { it.tenantId != tenantId }.toImmutableList(),
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
    val tenants: ImmutableList<Tenant> = persistentListOf(), // Guaranteed stable by Compose
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