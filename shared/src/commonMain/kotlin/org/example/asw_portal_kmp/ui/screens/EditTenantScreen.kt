package org.example.asw_portal_kmp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.asw_portal_kmp.network.api.tenants.Tenant
import org.example.asw_portal_kmp.ui.viewModels.EditTenantEvent
import org.example.asw_portal_kmp.ui.viewModels.EditTenantScreenViewModel
import org.example.asw_portal_kmp.ui.viewModels.EditTenantState

@Composable
fun EditTenantScreen(
    tenant: Tenant,
    onUpdateSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {

    val viewModel: EditTenantScreenViewModel = viewModel { EditTenantScreenViewModel(tenant) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                EditTenantEvent.UpdateSuccess -> onUpdateSuccess()
                EditTenantEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    val onEditNameChange = remember(viewModel) { { name: String -> viewModel.updateName(name) } }
    val onDomainChange = remember(viewModel) { { domain: String -> viewModel.updateDomain(domain) } }
    val onCustomDomainChange = remember(viewModel) { { customDomain: String -> viewModel.updateCustomDomain(customDomain) } }
    val onSubmit = remember(viewModel) { { viewModel.updateTenant() } }

    EditTenantScreenContent(
        state = state,
        onNameChange = onEditNameChange,
        onDomainChange = onDomainChange,
        onCustomDomainChange = onCustomDomainChange,
        onSubmit = onSubmit,
        onNavigateBack = onNavigateBack,
    )

}

@Composable
fun EditTenantScreenContent(
    state: EditTenantState,
    onNameChange: (String) -> Unit,
    onDomainChange: (String) -> Unit,
    onCustomDomainChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Tenant") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isSuccess) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SuccessContent(
                        message = "Tenant updated successfully!",
                        onContinue = onNavigateBack
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Edit Tenant Details",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Update the details below",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        label = { Text("Tenant Name") },
                        placeholder = { Text("e.g., Acme Corporation") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        singleLine = true,
                        isError = state.nameError != null,
                        supportingText = {
                            if (state.nameError != null) {
                                Text(
                                    text = state.nameError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = state.domain,
                        onValueChange = onDomainChange,
                        label = { Text("Subdomain") },
                        placeholder = { Text("e.g., acme") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        singleLine = true,
                        isError = state.domainError != null,
                        supportingText = {
                            if (state.domainError != null) {
                                Text(
                                    text = state.domainError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (state.domain.isNotBlank()) {
                                Text(
                                    text = "Tenant URL: ${state.domain}.yourapp.com",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = state.customDomain ?: "",
                        onValueChange = onCustomDomainChange,
                        label = { Text("Custom Domain (Optional)") },
                        placeholder = { Text("e.g., portal.mycompany.com") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        singleLine = true,
                        isError = state.customDomainError != null,
                        supportingText = {
                            if (state.customDomainError != null) {
                                Text(
                                    text = state.customDomainError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (!state.customDomain.isNullOrBlank()) {
                                Text(
                                    text = "Custom domain: ${state.customDomain}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    if (state.generalError != null) {
                        ErrorCard(message = state.generalError)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !state.isLoading &&
                                state.name.isNotBlank() &&
                                state.domain.isNotBlank() &&
                                state.domainError == null
                    ) {
                        if (state.isLoading) {
                            LoadingButtonContent(text = "Updating Tenant...")
                        } else {
                            Text(
                                text = "Update Tenant",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun SuccessContent(message: String, onContinue: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun LoadingButtonContent(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text)
    }
}

@Preview
@Composable
fun EditTenantScreenPreview() {
    EditTenantScreenContent(
        state = EditTenantState(isSuccess = false),
        onNameChange = {},
        onDomainChange = {},
        onCustomDomainChange = {},
        onSubmit = {},
        onNavigateBack = {}
    )
}