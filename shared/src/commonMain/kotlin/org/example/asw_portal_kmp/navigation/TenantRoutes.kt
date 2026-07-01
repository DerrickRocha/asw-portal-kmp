package org.example.asw_portal_kmp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface TenantRoute: NavKey {

    @Serializable
    data object TenantSelection: TenantRoute

    @Serializable
    data class TenantConsole(val tenantId: Int): TenantRoute

    @Serializable
    data object CreateTenant:TenantRoute

    @Serializable
    data object Products:TenantRoute

    @Serializable
    data object Orders:TenantRoute
    @Serializable
    data object Inventory:TenantRoute
    @Serializable
    data object InventoryItemDetails:TenantRoute
    @Serializable
    data object Settings:TenantRoute
    @Serializable
    data object Profile:TenantRoute
    @Serializable
    data object Help:TenantRoute
    @Serializable
    data object About:TenantRoute


}

@OptIn(ExperimentalSerializationApi::class)
private val navigationConfigTenants: SavedStateConfiguration
    get() = SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclassesOfSealed<TenantRoute>()
            }
        }
    }

@Composable
fun rememberTenantNavBackStack(initialRoute: TenantRoute): NavBackStack<NavKey> {
    return rememberNavBackStack(navigationConfigTenants, initialRoute)
}