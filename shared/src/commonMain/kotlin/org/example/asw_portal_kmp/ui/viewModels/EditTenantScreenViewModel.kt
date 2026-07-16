package org.example.asw_portal_kmp.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.Dependencies
import org.example.asw_portal_kmp.data.repositories.RepositoryResult
import org.example.asw_portal_kmp.data.network.tenants.NetworkTenant
import org.example.asw_portal_kmp.data.repositories.tenants.TenantsRepository

class EditTenantScreenViewModel(
    private val networkTenant: NetworkTenant,
    private val tenantRepository: TenantsRepository = Dependencies.tenantsRepository
): ViewModel() {

    private val _state = MutableStateFlow(
        EditTenantState(
            name = networkTenant.name,
            domain = networkTenant.subDomain,
            customDomain = networkTenant.customDomain ?: ""
        )
    )
    val state: StateFlow<EditTenantState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<EditTenantEvent>()
    val events: SharedFlow<EditTenantEvent> = _events.asSharedFlow()

    fun updateName(name: String) {
        _state.update {
            it.copy(
                name = name,
                nameError = if (name.isBlank()) "Name is required" else null
            )
        }
    }

    fun updateDomain(domain: String) {
        _state.update {
            it.copy(
                domain = domain.lowercase(),
                domainError = when {
                    domain.isBlank() -> "Subdomain is required"
                    !domain.matches(Regex("^[a-z0-9-]+$")) -> "Only lowercase letters, numbers, and hyphens allowed"
                    else -> null
                }
            )
        }
    }

    fun updateCustomDomain(customDomain: String) {
        _state.update {
            it.copy(
                customDomain = customDomain,
                customDomainError = when {
                    customDomain.isNotBlank() && !customDomain.matches(Regex("^[a-zA-Z0-9.-]+$")) -> "Invalid domain format"
                    else -> null
                }
            )
        }
    }

    fun updateTenant() {
        viewModelScope.launch {
            val currentState = _state.value

            // Validate
            val nameError = if (currentState.name.isBlank()) "Name is required" else null
            val domainError = if (currentState.domain.isBlank()) "Subdomain is required" else null

            if (nameError != null || domainError != null) {
                _state.update {
                    it.copy(
                        nameError = nameError,
                        domainError = domainError
                    )
                }
                return@launch
            }

            _state.update { it.copy(isLoading = true, generalError = null) }

            try {
                val updatedTenant = networkTenant.copy(
                    name = currentState.name,
                    subDomain = currentState.domain,
                    customDomain = currentState.customDomain.takeIf { it.isNotBlank() }
                )
                val result = tenantRepository.updateTenant(updatedTenant)
                when (result) {
                    is RepositoryResult.Failure -> {
                        _state.update { it.copy(isLoading = false, generalError = result.message) }
                    }
                    is RepositoryResult.Success<Unit> -> {
                        _state.update { it.copy(isLoading = false, isSuccess = true) }
                        _events.emit(EditTenantEvent.UpdateSuccess)
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        generalError = e.message ?: "Failed to update tenant"
                    )
                }
            }
        }
    }
}

data class EditTenantState(
    val name: String = "",
    val domain: String = "",
    val customDomain: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val nameError: String? = null,
    val domainError: String? = null,
    val customDomainError: String? = null,
    val generalError: String? = null
)

sealed class EditTenantEvent {
    object UpdateSuccess : EditTenantEvent()
    object NavigateBack : EditTenantEvent()
}