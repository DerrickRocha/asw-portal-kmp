package org.example.asw_portal_kmp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.example.asw_portal_kmp.Dependencies.kvManager
import org.example.asw_portal_kmp.navigation.Route
import org.example.asw_portal_kmp.navigation.rememberECommerceNavBackStack
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

                    is AppEffects.NavigateToTenantConsole -> {
                        backStack.clear()
                        backStack.add(Route.TenantConsole(effect.tenantId))
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
                                TenantSelectionScreen(
                                    onNavigateToTenantConsole = { tenantId ->
                                        backStack.clear()
                                        backStack.add(Route.TenantConsole(tenantId))
                                    },
                                    onNavigateToCreateTenant = { backStack.add(Route.CreateTenant) })
                            })
                        Route.CreateTenant -> NavEntry(key = key, content = {
                            AddTenantScreen(onContinueClicked = {
                                backStack.removeLast()
                            })
                        })

                        is Route.TenantConsole -> NavEntry(key = key, content = { Text("Tenant Console") })
                        else -> NavEntry(key = key, content = { Text("Unknown") })
                    }
                }
            )
        }
    }
}