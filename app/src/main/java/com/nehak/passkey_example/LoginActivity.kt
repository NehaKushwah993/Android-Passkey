package com.nehak.passkey_example

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.lifecycle.lifecycleScope
import com.nehak.passkey_example.ui.theme.PassKeyExampleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PassKeyExampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreenUI()
                }
            }
        }
    }
}

@Composable
fun LoginScreenUI() {

    var loginStatus by remember { mutableStateOf("") }
    val credentialManager = CredentialManager.create(LocalContext.current)
    val context = LocalContext.current

    // UI Elements for login (Username & Password)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            textAlign = TextAlign.Center,
            text = "If credentials exists in credential manager then it will work, otherwise manual login will be presented"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login button
        Button({
            loginStatus = "Attempting login..."

            // Launch a coroutine to handle credential request
            (context as ComponentActivity).lifecycleScope.launch {
                try {
                    val response = credentialManager.getCredential(
                        context = context,
                        GetCredentialRequest(credentialOptions = listOf(GetPasswordOption()))
                    )

                    val credential = response.credential
                    if (credential is PasswordCredential) {
                        // successful login
                        loginStatus = "Login Successful with existing credentials: ${credential.id}"
                        delay(1000L)
                        loginStatus = ""
                        context.startActivity(Intent(context, MainActivity::class.java))
                    }
                } catch (_: Exception) {
                    loginStatus = "No existing credentials, login with credentials first..."
                    // then login
                    delay(1000L)
                    loginStatus = ""
                    context.startActivity(Intent(context, SignupActivity::class.java))
                }

            }

        }) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = loginStatus, textAlign = TextAlign.Center)

    }


}


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    PassKeyExampleTheme {
        LoginScreenUI()
    }
}