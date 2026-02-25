package com.nogeass.passalarm.domain.service

import kotlinx.coroutines.flow.Flow

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
)

interface AuthService {
    val currentUser: AuthUser?
    suspend fun signInWithGoogle(activityContext: android.app.Activity): AuthUser
    suspend fun signOut()
    suspend fun getIDToken(): String
    fun observeAuthState(): Flow<AuthUser?>
}
