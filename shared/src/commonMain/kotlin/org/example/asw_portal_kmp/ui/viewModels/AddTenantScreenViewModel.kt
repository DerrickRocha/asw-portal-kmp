package org.example.asw_portal_kmp.ui.viewModels

import androidx.lifecycle.ViewModel

class AddTenantScreenViewModel: ViewModel() {
}

data class AddTenantState(
    val name: String = "",
    val domain: String = "",
    val customDomain: String = "",
    val isLoading: Boolean = false,
    val isCheckingDomain: Boolean = false,
    val isDomainAvailable: Boolean = false,
    val isDomainChecked: Boolean = false,
    val nameError: String? = null,
    val domainError: String? = null,
    val customDomainError: String? = null,
    val generalError: String? = null,
    val snackbarMessage: String? = null
)

sealed class AddTenantEvent {
    data class TenantCreated(val tenantId: Int) : AddTenantEvent()
    object NavigateBack : AddTenantEvent()
}