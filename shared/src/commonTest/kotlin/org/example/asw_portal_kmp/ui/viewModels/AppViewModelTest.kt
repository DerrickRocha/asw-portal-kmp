package org.example.asw_portal_kmp.ui.viewModels

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.asw_portal_kmp.data.KeyValuePairManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppViewModelTest {

    private val mockKeyValueManager = mock<KeyValuePairManager>(mode = MockMode.autofill)

    @Test
    fun testInit_whenUserLoggedInAndTenantIdExists_thenNavigateToTenantConsole() = runTest {
        everySuspend { mockKeyValueManager.isLoggedIn } returns flowOf(true)
        everySuspend { mockKeyValueManager.getTenantId() } returns 123

        val viewModel = AppViewModel(mockKeyValueManager)
        val effect = viewModel.effects.first()
        assertTrue(effect is AppEffects.NavigateToTenantConsole)
        assertEquals(123, effect.tenantId)
        verifySuspend { mockKeyValueManager.getTenantId() }
    }

    @Test
    fun testInit_whenUserLoggedInAndTenantIdDoesNotExist_thenNavigateToTenantSelection() = runTest {
        everySuspend { mockKeyValueManager.isLoggedIn } returns flowOf(true)
        everySuspend { mockKeyValueManager.getTenantId() } returns null
        val viewModel = AppViewModel(mockKeyValueManager)
        val effect = viewModel.effects.first()
        assertTrue(effect is AppEffects.NavigateToTenantSelection)
    }

    @Test
    fun testInit_whenUserLoggedInAndTenantIdIsNegative_thenNavigateToTenantSelection() = runTest {
        everySuspend { mockKeyValueManager.isLoggedIn } returns flowOf(true)
        everySuspend { mockKeyValueManager.getTenantId() } returns -1
        val viewModel = AppViewModel(mockKeyValueManager)
        val effect = viewModel.effects.first()
        assertTrue(effect is AppEffects.NavigateToTenantSelection)
    }

    @Test
    fun testInit_whenUserNotLoggedIn_thenNavigateToLogin() = runTest {
        everySuspend { mockKeyValueManager.isLoggedIn } returns flowOf(false)
        val viewModel = AppViewModel(mockKeyValueManager)
        val effect = viewModel.effects.first()
        assertTrue(effect is AppEffects.NavigateToLogin)
    }
}