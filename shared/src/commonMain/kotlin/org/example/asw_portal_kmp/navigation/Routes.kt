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
sealed interface Route: NavKey {

    @Serializable
    data object Splash: Route

    @Serializable
    data object Login: Route

    @Serializable
    data object Signup: Route
    @Serializable
    data class PinScreen(val email: String): Route

    @Serializable
    data object TenantSelection: Route
    @Serializable
    data class TenantConsole(val tenantId: Int): Route
    @Serializable
    data object Products: Route
    @Serializable
    data class ProductDetails(val productId: Int): Route
    @Serializable
    data object Orders: Route
    @Serializable
    data class OrderDetails(val orderId: Int): Route
    @Serializable
    data object Inventories: Route
    @Serializable
    data class InventoryItemDetails(val inventoryId: Int): Route

    @Serializable
    data object CreateTenant: Route
}

@OptIn(ExperimentalSerializationApi::class)
val navigationConfig: SavedStateConfiguration
    get() = SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclassesOfSealed<Route>()
            }
        }
    }

@Composable
fun rememberECommerceNavBackStack(initialRoute: Route): NavBackStack<NavKey> {
    return rememberNavBackStack(navigationConfig, initialRoute)
}
