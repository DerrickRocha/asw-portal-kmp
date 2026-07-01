package org.example.asw_portal_kmp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import org.example.asw_portal_kmp.Dependencies.kvManager
import org.example.asw_portal_kmp.navigation.Route
import org.example.asw_portal_kmp.navigation.TenantRoute
import org.example.asw_portal_kmp.navigation.rememberECommerceNavBackStack
import org.example.asw_portal_kmp.navigation.rememberTenantNavBackStack
import org.example.asw_portal_kmp.ui.screens.AddTenantScreen
import org.example.asw_portal_kmp.ui.screens.LoginScreen
import org.example.asw_portal_kmp.ui.screens.PinScreen
import org.example.asw_portal_kmp.ui.screens.SignupScreen
import org.example.asw_portal_kmp.ui.screens.TenantSelectionScreen
import org.example.asw_portal_kmp.ui.viewModels.AppEffects
import org.example.asw_portal_kmp.ui.viewModels.AppViewModel
import kotlin.collections.listOf

@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel: AppViewModel = viewModel {
            AppViewModel(keyValuePairManager = kvManager)
        }
        val backStack = rememberECommerceNavBackStack(Route.Splash)

        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is AppEffects.NavigateToLogin -> {
                        backStack.clear()
                        backStack.add(Route.Login)
                    }

                    AppEffects.NavigateToTenantSelection -> {
                        backStack.clear()
                        backStack.add(Route.TenantSelection)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = { key ->
                    when (key) {
                        Route.Splash -> NavEntry(key = key, content = { Text("Splash") })
                        Route.Login -> NavEntry(
                            key = key,
                            content = {
                                LoginScreen(onNavigateToSignUp = {
                                    backStack.clear()
                                    backStack.add(Route.Signup)
                                })
                            })

                        Route.Signup -> NavEntry(
                            key = key,
                            content = {
                                SignupScreen(
                                    { email ->
                                        backStack.clear()
                                        backStack.add(Route.PinScreen(email))
                                    },
                                    onNavigateToLogin = {
                                        backStack.clear()
                                        backStack.add(Route.Login)
                                    })
                            })

                        is Route.PinScreen -> {
                            NavEntry(
                                key = key,
                                content = {
                                    PinScreen(
                                        key.email,
                                        onContinueClicked = {
                                            backStack.clear()
                                            backStack.add(Route.Login)
                                        },
                                        onNavigateBack = { backStack.removeLast() })
                                })
                        }

                        Route.TenantSelection -> NavEntry(
                            key = key,
                            content = {
                                TenantNavDisplay(viewModel::logout)
                            })

                        else -> NavEntry(key = key, content = { Text("Unknown") })
                    }
                }
            )
        }
    }
}

@Composable
fun TenantNavDisplay(onLogoutClick: () -> Unit) {

    val tenantsBackstack = rememberTenantNavBackStack(TenantRoute.TenantSelection)
    var refreshTrigger by remember { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("Agile Southwest Portal") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Agile Southwest Portal",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Tenant Management",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Drawer Items
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Tenants"
                        )
                    },
                    label = { Text("Tenants") },
                    selected = tenantsBackstack.lastOrNull() is TenantRoute.TenantSelection,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") },
                    selected = tenantsBackstack.lastOrNull() is TenantRoute.Profile,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            // Navigate to profile
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings") },
                    selected = tenantsBackstack.lastOrNull() is TenantRoute.Settings,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            // Navigate to settings
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Logout Item
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    label = {
                        Text(
                            text = "Logout",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onLogoutClick()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedTextColor = MaterialTheme.colorScheme.error,
                        selectedIconColor = MaterialTheme.colorScheme.error,
                        unselectedTextColor = MaterialTheme.colorScheme.error,
                        unselectedIconColor = MaterialTheme.colorScheme.error
                    )
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            modifier = Modifier.clickable {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        ) { paddingValues ->
            NavDisplay(
                modifier = Modifier.padding(paddingValues),
                backStack = tenantsBackstack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = { tenantKey ->
                    when (tenantKey) {
                        is TenantRoute.TenantSelection -> {
                            NavEntry(
                                key = tenantKey,
                                content = {
                                    title = "Tenant Selection"
                                    TenantSelectionScreen(
                                        onNavigateToTenantConsole = { tenantId ->
                                            tenantsBackstack.add(TenantRoute.TenantConsole(tenantId))
                                        },
                                        onNavigateToCreateTenant = { tenantsBackstack.add(TenantRoute.CreateTenant) },
                                        onNavigateToEditTenant = { tenant -> },
                                        refreshTrigger = refreshTrigger
                                    )
                                }
                            )
                        }

                        TenantRoute.CreateTenant -> NavEntry(key = tenantKey, content = {
                            title = "Create Tenant"
                            AddTenantScreen(
                                onContinueClicked = {
                                    refreshTrigger = !refreshTrigger
                                    tenantsBackstack.removeLast()
                                })
                        })

                        is TenantRoute.TenantConsole -> NavEntry(key = tenantKey, content = {
                            title = "Tenant Console"
                            Text("Tenant Console")
                        })

                        else -> NavEntry(
                            key = tenantKey,
                            content = {
                                title = "Unknown"
                                Text("Unknown")
                            }
                        )
                    }
                }
            )
        }
    }

}