package com.nogeass.passalarm.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nogeass.passalarm.R
import com.nogeass.passalarm.domain.service.AuthService
import com.nogeass.passalarm.domain.service.AuthUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AuthService {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.let { user ->
            AuthUser(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
            )
        }

    override suspend fun signInWithGoogle(activityContext: Activity): AuthUser {
        val credentialManager = CredentialManager.create(activityContext)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(activityContext.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            context = activityContext,
            request = request,
        )

        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(
            result.credential.data,
        )
        val idToken = googleIdTokenCredential.idToken

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()

        val user = authResult.user
            ?: throw IllegalStateException("Firebase sign-in succeeded but user is null")

        return AuthUser(
            uid = user.uid,
            email = user.email,
            displayName = user.displayName,
        )
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun getIDToken(): String {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("Not signed in")
        val tokenResult = user.getIdToken(false).await()
        return tokenResult.token
            ?: throw IllegalStateException("Failed to get ID token")
    }

    override fun observeAuthState(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser?.let {
                AuthUser(
                    uid = it.uid,
                    email = it.email,
                    displayName = it.displayName,
                )
            }
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
}
