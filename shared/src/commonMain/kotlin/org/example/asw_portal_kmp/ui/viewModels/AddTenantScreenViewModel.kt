package org.example.asw_portal_kmp.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.Dependencies
import org.example.asw_portal_kmp.network.api.RepositoryResult
import org.example.asw_portal_kmp.network.api.tenants.AddTenantResponse
import org.example.asw_portal_kmp.network.api.tenants.TenantsRepository

class AddTenantScreenViewModel(
    private val repository: TenantsRepository = Dependencies.tenantsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddTenantState())
    val state: StateFlow<AddTenantState> = _state.asStateFlow()

    private var createTenantJob: Job? = null

    fun updateName(name: String) {
        _state.value = _state.value.copy(
            name = name,
            nameError = null,
            generalError = null
        )
    }

    fun updateDomain(domain: String) {
        val sanitized = domain.lowercase().replace(Regex("[^a-z0-9-]"), "")
        _state.value = _state.value.copy(
            domain = sanitized,
            domainError = null,
            generalError = null,
        )
    }

    fun updateCustomDomain(customDomain: String) {
        val sanitized = customDomain.lowercase().trim()
        _state.value = _state.value.copy(
            customDomain = sanitized,
            customDomainError = null,
            generalError = null
        )
    }

    private fun isValidDomain(domain: String): Boolean {
        // Basic domain validation
        val domainRegex = Regex("^[a-zA-Z0-9]([a-zA-Z0-9-]*(\\.[a-zA-Z0-9-]+)*)\\.[a-zA-Z]{2,}$")
        return domainRegex.matches(domain)
    }

    fun createTenant() {
        val currentState = _state.value

        if (currentState.name.isBlank()) {
            _state.value = currentState.copy(
                nameError = "Tenant name is required"
            )
            return
        }

        if (currentState.domain.isBlank()) {
            _state.value = currentState.copy(
                domainError = "Subdomain is required"
            )
            return
        }

        if (currentState.domain.length < 3) {
            _state.value = currentState.copy(
                domainError = "Subdomain must be at least 3 characters"
            )
            return
        }

        if (currentState.domain.length > 63) {
            _state.value = currentState.copy(
                domainError = "Subdomain must be less than 63 characters"
            )
            return
        }

        if (currentState.customDomain.isNotBlank() && !isValidDomain(currentState.customDomain)) {
            _state.value = currentState.copy(
                customDomainError = "Please enter a valid domain (e.g., portal.mycompany.com)"
            )
            return
        }

        createTenantJob?.cancel()
        createTenantJob = viewModelScope.launch {
            _state.value = currentState.copy(
                isLoading = true,
                generalError = null,
                nameError = null,
                domainError = null,
                customDomainError = null,
                isSuccess = false,
            )

            try {
                val result = repository.createTenant(
                    name = currentState.name,
                    domain = currentState.domain,
                    customDomain = currentState.customDomain.takeIf { it.isNotBlank() }
                )

                when (result) {
                    is RepositoryResult.Failure -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            generalError = result.message
                        )
                    }

                    is RepositoryResult.Success<AddTenantResponse> -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            generalError = null,
                            isSuccess = true,
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    generalError = e.message ?: "An error occurred"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        createTenantJob?.cancel()
    }
}

data class AddTenantState(
    val name: String = "",
    val domain: String = "",
    val customDomain: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val domainError: String? = null,
    val customDomainError: String? = null,
    val generalError: String? = null,
    val snackbarMessage: String? = null,
    val isSuccess: Boolean = false,
)