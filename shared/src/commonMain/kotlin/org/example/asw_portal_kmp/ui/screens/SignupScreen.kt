package org.example.asw_portal_kmp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.asw_portal_kmp.ui.viewModels.SignupEvent
import org.example.asw_portal_kmp.ui.viewModels.SignupScreenState
import org.example.asw_portal_kmp.ui.viewModels.SignupViewModel

@Composable
fun SignupScreen(
    onNavigateToPinScreen: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: SignupViewModel = viewModel { SignupViewModel() }
    val state by viewModel.state.collectAsState()

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SignupEvent.NavigateToPinScreen -> {
                    onNavigateToPinScreen(event.email)
                }
                SignupEvent.NavigateToLogin -> {
                    onNavigateToLogin()
                }
            }
        }
    }

    SignupScreenSection(
        state = state,
        onUpdateEmail = viewModel::updateEmail,
        onUpdatePassword = viewModel::updatePassword,
        onUpdateConfirmPassword = viewModel::updateConfirmPassword,
        onUpdateSubdomain = viewModel::updateSubdomain,
        onUpdateCustomDomain = viewModel::updateCustomDomain,
        onSignUpClick = viewModel::signUp,
        onNavigateToLogin = onNavigateToLogin,
        onUpdateCompanyName = viewModel::updateCompanyName
    )
}

@Composable
fun SignupScreenSection(
    state: SignupScreenState,
    onUpdateCompanyName: (String) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onUpdateConfirmPassword: (String) -> Unit,
    onUpdateSubdomain: (String) -> Unit,
    onUpdateCustomDomain: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val subdomainFocusRequester = remember { FocusRequester() }
    val customDomainFocusRequester = remember { FocusRequester() }
    val companyNameFocusRequester = remember { FocusRequester() }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Create Your Account",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
            )

            Text(
                text = "Sign up to get started with Agile Southwest Portal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Company Name Field (Optional)
            OutlinedTextField(
                value = state.companyName,
                onValueChange = onUpdateCompanyName,
                label = { Text("Company Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(companyNameFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSignUpClick() }
                ),
                enabled = !state.isLoading,
                singleLine = true,
                isError = state.companyNameError != null
            )

            if (state.companyNameError != null) {
                Text(
                    text = state.companyNameError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            if (state.generalError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.generalError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Email Field
            OutlinedTextField(
                value = state.email,
                onValueChange = onUpdateEmail,
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                enabled = !state.isLoading,
                singleLine = true,
                isError = state.emailError != null
            )

            if (state.emailError != null) {
                Text(
                    text = state.emailError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = state.password,
                onValueChange = onUpdatePassword,
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { confirmPasswordFocusRequester.requestFocus() }
                ),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                enabled = !state.isLoading,
                singleLine = true,
                isError = state.passwordError != null
            )

            if (state.passwordError != null) {
                Text(
                    text = state.passwordError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password Field
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = onUpdateConfirmPassword,
                label = { Text("Confirm Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmPasswordFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { subdomainFocusRequester.requestFocus() }
                ),
                visualTransformation = if (confirmPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                enabled = !state.isLoading,
                singleLine = true,
                isError = state.confirmPasswordError != null
            )

            if (state.confirmPasswordError != null) {
                Text(
                    text = state.confirmPasswordError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subdomain Field
            OutlinedTextField(
                value = state.subdomain,
                onValueChange = onUpdateSubdomain,
                label = { Text("Subdomain") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(subdomainFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { customDomainFocusRequester.requestFocus() }
                ),
                enabled = !state.isLoading,
                singleLine = true,
                isError = state.subdomainError != null,
                trailingIcon = {
                    if (state.subdomain.isNotBlank()) {
                        Text(
                            text = ".yourapp.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )

            if (state.subdomainError != null) {
                Text(
                    text = state.subdomainError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Domain Field (Optional)
            OutlinedTextField(
                value = state.customDomain,
                onValueChange = onUpdateCustomDomain,
                label = { Text("Custom Domain (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(customDomainFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSignUpClick() }
                ),
                enabled = !state.isLoading,
                singleLine = true,
                isError = state.customDomainError != null,
                placeholder = { Text("e.g., portal.yourcompany.com") }
            )

            if (state.customDomainError != null) {
                Text(
                    text = state.customDomainError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            if (state.generalError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.generalError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Button
            Button(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Sign up button" },
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating Account...")
                    }
                } else {
                    Text("Sign Up")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Already have an account? Login link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    enabled = !state.isLoading,
                    modifier = Modifier.semantics {
                        contentDescription = "Login button"
                    }
                ) {
                    Text(
                        text = "Login",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}