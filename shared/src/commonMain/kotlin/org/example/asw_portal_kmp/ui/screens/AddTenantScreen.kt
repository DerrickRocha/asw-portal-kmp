package org.example.asw_portal_kmp.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.asw_portal_kmp.ui.viewModels.AddTenantScreenViewModel
import org.example.asw_portal_kmp.ui.viewModels.AddTenantState

@Composable
fun AddTenantScreen(
    onContinueClicked: () -> Unit,
) {
    val viewModel: AddTenantScreenViewModel = viewModel {
        AddTenantScreenViewModel()
    }

    val state by viewModel.state.collectAsState()

    AddTenantScreenContent(
        state = state,
        onNameChange = viewModel::updateName,
        onDomainChange = viewModel::updateDomain,
        onCustomDomainChange = viewModel::updateCustomDomain,
        onSubmit = viewModel::createTenant,
        onContinueClicked = onContinueClicked,
    )
}

@Composable
fun AddTenantScreenContent(
    state: AddTenantState,
    onNameChange: (String) -> Unit,
    onDomainChange: (String) -> Unit,
    onCustomDomainChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onContinueClicked: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (state.isSuccess) Arrangement.Center else Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (state.isSuccess) {
                Text(
                    text = "New Tenant Added Successfully!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Your tenant has been created.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Manual continue button
                Button(
                    onClick = onContinueClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Text("Continue")
                }
            } else {

                // Header
                Text(
                    text = "Create a New Tenant",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Fill in the details below to create your tenant",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Tenant Name Field
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
                    },
                    trailingIcon = {
                        if (state.name.isNotBlank()) {
                            IconButton(
                                onClick = { onNameChange("") },
                                enabled = !state.isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )

                // Domain Field
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
                                text = state.domainError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (state.domain.isNotBlank()) {
                            Text(
                                text = "Your tenant URL will be: ${state.domain}.yourapp.com",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                // Custom Domain Field (Optional)
                OutlinedTextField(
                    value = state.customDomain,
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
                                text = state.customDomainError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (state.customDomain.isNotBlank()) {
                            Text(
                                text = "Your custom domain will be: $state.customDomain",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Error Message
                if (state.generalError != null) {
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
                                text = state.generalError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Submit Button
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
                            Text("Creating Tenant...")
                        }
                    } else {
                        Text(
                            text = "Create Tenant",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Info Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "What's a Tenant?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "A tenant is a separate workspace for your organization. Each tenant has its own users, projects, and settings. Your subdomain will be used to access your tenant portal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview
@Composable
fun AddTenantScreenPreview() {
    AddTenantScreenContent(
        state = AddTenantState(isSuccess = true),
        onNameChange = {},
        onDomainChange = {},
        onCustomDomainChange = {},
        onSubmit = {},
        onContinueClicked = {  },
    )
}