package com.nehak.passkey_example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialNoCreateOptionException
import androidx.lifecycle.lifecycleScope
import com.nehak.passkey_example.ui.theme.PassKeyExampleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

class SignupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PassKeyExampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SignupScreenUI()
                }
            }
        }
    }
}

@Composable
fun SignupScreenUI() {

    var signupStatus by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val credentialManager = CredentialManager.create(LocalContext.current)
    val context = LocalContext.current

    // UI Elements for Signup (Username & Password)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(text = "Signup", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Username TextField
        TextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("Username") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password TextField
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation() // Hides the password
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Signup button
        Button({
            signupStatus = "Attempting Signup..."

            // Launch a coroutine to handle credential request
            (context as ComponentActivity).lifecycleScope.launch {

                // Credential doesn't exist, so create one and save
                try {
                    saveCredentials(context, userId, password)
                } catch (e: Exception) {
                    signupStatus = e.message ?: ""
                    return@launch
                }
                // then Signup
                delay(1000L)
                context.finish()

            }

        }) {
            Text(text = "Store my creds")
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text(text = signupStatus, textAlign = TextAlign.Center)

    }


}

private suspend fun saveCredentials(context: Context, userId: String, password: String) {
    val credentialManager = CredentialManager.create(context)

    try {
        // Save the new password credentials using the correct CreatePasswordRequest
        credentialManager.createCredential(
            request = CreatePasswordRequest(userId, password),
            context = context // Pass the context as required
        )

        // Successfully saved credentials
        println("Credentials saved successfully.")
    } catch (e: CreateCredentialNoCreateOptionException) {
        // Handle exception if no create options are available
        throw Exception("Error: No providers available for saving credentials. ${e.message}")
    } catch (e: Exception) {
        // Handle other exceptions
        throw Exception("Error: ${e.message}")
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    PassKeyExampleTheme {
        SignupScreenUI()
    }
}