package org.example.asw_portal_kmp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.asw_portal_kmp.data.KeyValuePairManagerImplementation
import org.example.asw_portal_kmp.data.createDataStore
import org.example.asw_portal_kmp.getPlatform
import org.example.asw_portal_kmp.network.NetworkConfig
import org.example.asw_portal_kmp.network.NetworkManager
import org.example.asw_portal_kmp.network.api.auth.AuthRepository
import org.example.asw_portal_kmp.network.api.auth.AuthRepositoryImpl
import org.example.asw_portal_kmp.ui.viewModels.LoginScreenState
import org.example.asw_portal_kmp.ui.viewModels.LoginScreenViewModel

private val platform = getPlatform()
private val networkConfig = NetworkConfig()
private val client = HttpClient(){
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
    defaultRequest {
        val baseUrl = networkConfig.getBaseUrl()
        println("Base URL: $baseUrl")
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }
    expectSuccess = true
}
val store = createDataStore()
private val kvManager = KeyValuePairManagerImplementation(store)
private val networkManager = NetworkManager(client, kvManager)
private val api: AuthRepository = AuthRepositoryImpl(networkManager, kvManager)
@Composable
fun LoginScreen() {
    val viewModel: LoginScreenViewModel = viewModel { LoginScreenViewModel(api) }
    val state by viewModel.state.collectAsState()
    LoginScreenSection(state,
        viewModel::updateUsername,
        viewModel::updatePassword,
        viewModel::login
    )
}

@Composable
fun LoginScreenSection(
    state: LoginScreenState,
    onUpdateUserName: (String) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Agile Southwest Portal",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = state.username,
                    onValueChange = onUpdateUserName,
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = onUpdatePassword,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                )

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Login")
                    }
                }
            }
        }
    }
}