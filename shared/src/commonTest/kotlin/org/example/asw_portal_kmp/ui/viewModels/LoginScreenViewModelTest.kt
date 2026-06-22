package org.example.asw_portal_kmp.ui.viewModels

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.Answer
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.example.asw_portal_kmp.network.api.auth.AuthRepository
import org.example.asw_portal_kmp.network.api.auth.LoginResult
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginScreenViewModelTest {

    private lateinit var repository: AuthRepository
    private lateinit var viewModel: LoginScreenViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        repository = mock<AuthRepository>()
        viewModel = LoginScreenViewModel(repository, testDispatcher)
    }

    // ========== INITIAL STATE TESTS ==========

    @Test
    fun `initial state should be empty`() {
        // Arrange & Act
        val state = viewModel.state.value

        // Assert
        assertEquals("", state.username)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
    }

    // ========== INPUT UPDATE TESTS ==========

    @Test
    fun `updateUsername should update username and clear error and success`() = runTest {
        // Arrange
        viewModel.updateUsername("testuser")

        // Act
        viewModel.updateUsername("newuser")

        // Assert
        val state = viewModel.state.value
        assertEquals("newuser", state.username)
        assertNull(state.error)
        assertFalse(state.isSuccess)
    }

    @Test
    fun updateUsername_shouldClearErrorAndSuccessStates() = runTest {
        // Arrange
        val errorMessage = "Error"
        everySuspend { repository.login(any(), any()) } returns LoginResult.Failure(errorMessage)

       // viewModel = LoginScreenViewModel(repository, testDispatcher)
        viewModel.updateUsername("testuser")
        viewModel.updatePassword("password")

        // Act - Login should fail
        viewModel.login()

        // Use yield to let coroutines process
        advanceUntilIdle()

        // Wait for the coroutine to complete
        // Assert error is set
        var state = viewModel.state.value
        assertNotNull(state.error)
        assertFalse(state.isSuccess)
        assertEquals(errorMessage, state.error)

        // Act - Update username
        viewModel.updateUsername("newuser")

        // Assert - Error and success should be cleared
        state = viewModel.state.value
        assertEquals("newuser", state.username)
        assertNull(state.error)
        assertFalse(state.isSuccess)
        assertFalse(state.isLoading)
    }

    @Test
    fun updatePassword_shouldClearErrorAndSuccessStates() = runTest {
        // Arrange
        everySuspend { repository.login(any(), any()) } returns LoginResult.Failure("Error")
        viewModel.updateUsername("testuser")
        viewModel.updatePassword("password")

        // Act - Login should fail
        viewModel.login()
        advanceUntilIdle()

        // Verify error is set
        var state = viewModel.state.value
        assertNotNull(state.error)

        // Act - Update password
        viewModel.updatePassword("newpassword")

        // Assert - Error and success should be cleared
        state = viewModel.state.value
        assertEquals("newpassword", state.password)
        assertNull(state.error)
        assertFalse(state.isSuccess)
        assertFalse(state.isLoading)
    }

    @Test
    fun testLogin_whenEmailAndPasswordValid_shouldSetSuccessState() = runTest {
        everySuspend { repository.login(any(), any()) } returns LoginResult.Success

        viewModel.updateUsername("testuser")
        viewModel.updatePassword("password")

        // Act - Login should fail
        viewModel.login()
        advanceUntilIdle()

        // Verify error is set
        val state = viewModel.state.value

        assertTrue(state.isSuccess && !state.isLoading && state.error == null)
    }

}