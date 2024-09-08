package com.nehak.passkey_example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.nehak.passkey_example.ui.theme.PassKeyExampleTheme
import kotlinx.coroutines.launch

// Main activity that sets the content and theme for the login screen
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PassKeyExampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

// Main composable function for the login screen
@Composable
fun LoginScreen() {
    // State variables for credential status, user ID, and password
    var credentialState by remember { mutableStateOf<CredentialState>(CredentialState.Loading) }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Get the context and create a CredentialManager instance
    val context = LocalContext.current
    val credentialManager = CredentialManager.create(context)

    // LaunchedEffect to retrieve credentials and handle login logic
    LaunchedEffect(Unit) {
        retrieveCredentials(context, credentialManager) { state ->
            credentialState = state
            if (state is CredentialState.Success) {
                userId = state.userId
                password = state.password
                context.startActivity(Intent(context, MainActivity::class.java))
            }
        }
    }

    // Layout for the login screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Information text for credential management
        Text(
            textAlign = TextAlign.Center,
            text = "If credentials exist, a popup will appear for selection. If not, perform manual login to save credentials."
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Heading for the login screen
        Text(text = "Login", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Username input field
        TextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("Username") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password input field with hidden characters
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Show a loading spinner if credentials are being retrieved
        if (credentialState is CredentialState.Loading) {
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to perform login and save credentials
        Button(onClick = {
            (context as ComponentActivity).lifecycleScope.launch {
                saveCredentials(context, userId, password, onSaveCredentialStateChanged = {
                    credentialState = it
                    if (it is CredentialState.Success) {

                        // Login success
                        context.startActivity(Intent(context, MainActivity::class.java))
                    }
                })
            }
        }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display status message based on the credential state
        when (credentialState) {
            is CredentialState.Success -> {
                Text(
                    text = "Login successful with user ID: ${(credentialState as CredentialState.Success).userId}",
                    textAlign = TextAlign.Center
                )
            }

            is CredentialState.Error -> {
                Text(
                    text = (credentialState as CredentialState.Error).message,
                    textAlign = TextAlign.Center
                )
            }

            else -> Unit
        }
    }
}

// Preview of the LoginScreen composable
@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    PassKeyExampleTheme {
        LoginScreen()
    }
}
