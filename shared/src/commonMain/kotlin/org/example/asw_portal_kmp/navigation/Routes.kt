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

}

@OptIn(ExperimentalSerializationApi::class)
private val navigationConfig: SavedStateConfiguration
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
