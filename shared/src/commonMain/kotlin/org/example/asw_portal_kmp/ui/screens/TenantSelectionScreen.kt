package org.example.asw_portal_kmp.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.asw_portal_kmp.network.api.tenants.Tenant
import org.example.asw_portal_kmp.ui.viewModels.TenantSelectionEvent
import org.example.asw_portal_kmp.ui.viewModels.TenantSelectionState
import org.example.asw_portal_kmp.ui.viewModels.TenantSelectionViewModel
import org.example.asw_portal_kmp.utils.DateUtils

@Composable
fun TenantSelectionScreen(
    onNavigateToTenantConsole: (Int) -> Unit,
    onNavigateToCreateTenant: () -> Unit,
    refreshTrigger: Boolean = false
) {
    val viewModel: TenantSelectionViewModel = viewModel {
        TenantSelectionViewModel()
    }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(refreshTrigger) {
        viewModel.loadTenants()
        viewModel.events.collect { event ->
            when (event) {
                is TenantSelectionEvent.NavigateToTenantConsole -> {
                    onNavigateToTenantConsole(event.tenantId)
                }
            }
        }
    }

    TenantSelectionScreenContent(
        state = state,
        onTenantSelected = viewModel::selectTenant,
        onCreateTenantClick = onNavigateToCreateTenant,
        onRetryClick = viewModel::retry
    )
}

@Composable
fun TenantSelectionScreenContent(
    state: TenantSelectionState,
    onTenantSelected: (Tenant) -> Unit,
    onCreateTenantClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTenantClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Tenant"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingContent()
                }

                state.error != null -> {
                    ErrorContent(
                        error = state.error,
                        onRetry = onRetryClick
                    )
                }

                state.tenants.isEmpty() -> {
                    EmptyContent(
                        onCreateTenant = onCreateTenantClick
                    )
                }

                else -> {
                    TenantListContent(
                        tenants = state.tenants,
                        onTenantSelected = onTenantSelected
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading tenants...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Failed to load tenants",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyContent(
    onCreateTenant: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Business,
            contentDescription = "No tenants",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Tenants Found",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You don't have any tenants yet. Create your first tenant to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateTenant,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Create Tenant")
        }
    }
}

@Composable
fun TenantListContent(
    tenants: List<Tenant>,
    onTenantSelected: (Tenant) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tenants) { tenant ->
            TenantListItem(
                tenant = tenant,
                onClick = { onTenantSelected(tenant) }
            )
        }
    }
}

@Composable
fun TenantListItem(
    tenant: Tenant,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = tenant.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                val customDomain = if (tenant.customDomain.isNullOrBlank()) tenant.subDomain else tenant.customDomain
                Text(
                    text = "${customDomain}.agilesouthwest.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = DateUtils.getTimeAgo(tenant.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
fun TenantSelectionScreenPreview() {
    TenantSelectionScreenContent(
        state = TenantSelectionState(
            tenants = listOf(
                Tenant(
                    1,
                    "Tenant 1",
                    "tenant1.yourapp.com",
                    "tenant1.yourapp.com",
                    "2023-01-01T12:00:00Z",
                    "2023-01-02T12:00:00Z",
                    "2023-01-02T12:00:00Z"
                ),
                Tenant(
                    2,
                    "Tenant 2",
                    "tenant2.yourapp.com",
                    "tenant2.yourapp.com",
                    "2023-01-02T12:00:00Z",
                    "2023-01-02T12:00:00Z",
                    "2023-01-02T12:00:00Z"
                )
            )
        ),
        onTenantSelected = {},
        onCreateTenantClick = {},
        onRetryClick = {}
    )
}
