package com.nehak.passkey_example

import android.content.Context
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialNoCreateOptionException
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.delay

// Sealed class to represent different states of credential retrieval
sealed class CredentialState {
    // Indicates that credentials are being loaded
    data object Loading : CredentialState()

    // Indicates successful retrieval of credentials with user ID and password
    data class Success(val userId: String, val password: String) : CredentialState()

    // Indicates an error occurred with an error message
    data class Error(val message: String) : CredentialState()
}

/**
 * Retrieves user credentials from the Credential Manager.
 *
 * @param context The context used to interact with the Credential Manager.
 * @param credentialManager The CredentialManager instance responsible for handling credential requests.
 * @param onCredentialStateChanged Callback to notify the caller of the state changes during credential retrieval.
 *
 * @return Unit This function does not return a value. It uses the callback to report the credential retrieval state.
 *
 * @throws Exception If there is an error retrieving the credentials, such as no credentials being found or credential retrieval failing.
 */
suspend fun retrieveCredentials(
    context: Context,
    credentialManager: CredentialManager, // CredentialManager instance to handle credentials
    onCredentialStateChanged: (CredentialState) -> Unit // Callback to notify the caller of state changes
) {
    // Check if CredentialManager is supported on the device
    if (!isCredentialManagerAvailable(context)) {
        // Notify that CredentialManager is not supported
        onCredentialStateChanged(CredentialState.Error("CredentialManager is not supported on this device."))
        return
    }

    if (!isGooglePlayServicesAvailable(context)) {
        // Notify that Google Play Services is not enabled
        onCredentialStateChanged(CredentialState.Error("Google Play Services is not enabled on this device."))
        return
    }

    try {
        // Notify that credential retrieval is starting
        onCredentialStateChanged(CredentialState.Loading)

        // Request to get saved credentials
        val response = credentialManager.getCredential(
            context = context,
            GetCredentialRequest(credentialOptions = listOf(GetPasswordOption()))
        )

        // Extract the credential from the response
        val credential = response.credential

        // Check if the retrieved credential is a PasswordCredential
        if (credential is PasswordCredential) {
            // Notify success with the retrieved user ID and password
            onCredentialStateChanged(CredentialState.Success(credential.id, credential.password))
        }
    } catch (e: Exception) {
        // Handle any errors that occur during credential retrieval
        onCredentialStateChanged(CredentialState.Error("No credentials found. Please login manually to save credentials."))
    }
}

/**
 * Saves user credentials using the Credential Manager.
 *
 * @param context The context used to interact with the Credential Manager.
 * @param userId The user ID to be saved.
 * @param password The password to be saved.
 * @param onSaveCredentialStateChanged Callback to notify the caller of state changes.
 *
 * @throws Exception If there is an error saving the credentials or no providers are available.
 */
suspend fun saveCredentials(
    context: Context, userId: String, password: String,
    onSaveCredentialStateChanged: (CredentialState) -> Unit // Callback to notify the caller of state changes
) {
    // Notify that credential retrieval is starting
    onSaveCredentialStateChanged(CredentialState.Loading)

    // Create an instance of CredentialManager
    val credentialManager = CredentialManager.create(context)

    try {

        // Simulate a delay (e.g., for UI transitions or async operations)
        delay(1000L)

        // Request to create and save new credentials with the provided user ID and password
        credentialManager.createCredential(
            request = CreatePasswordRequest(
                userId,
                password
            ), // CreatePasswordRequest to specify the new credentials
            context = context // Pass the context to access system services
        )

        // Notify success with the retrieved user ID and password
        onSaveCredentialStateChanged(CredentialState.Success(userId, password))

    } catch (e: CreateCredentialNoCreateOptionException) {
        // Handle case where no providers are available to save credentials
        onSaveCredentialStateChanged(CredentialState.Error("No providers available to save credentials. ${e.message}"))
    } catch (e: Exception) {
        // Handle any other errors that occur during credential saving
        onSaveCredentialStateChanged(CredentialState.Error("Error saving credentials: ${e.message}"))
    }
}

/**
 * Check if CredentialManager is available on the device.
 *
 * @param context The application context.
 * @return True if CredentialManager is available, false otherwise.
 */
private fun isCredentialManagerAvailable(context: Context): Boolean {
    return try {
        // Attempt to create a CredentialManager instance to see if it is supported
        CredentialManager.create(context)
        true
    } catch (e: Exception) {
        // If an exception occurs, CredentialManager is not supported
        false
    }
}


/**
 * Function to check if Google Play Services is enabled
 *
 * @param context The application context.
 * @return True if is enabled, false otherwise.
 */
private fun isGooglePlayServicesAvailable(context: Context): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val status = googleApiAvailability.isGooglePlayServicesAvailable(context)
    return status == com.google.android.gms.common.ConnectionResult.SUCCESS
}