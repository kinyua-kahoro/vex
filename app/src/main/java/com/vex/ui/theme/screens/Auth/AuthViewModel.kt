package com.vex.ui.theme.screens.Auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.vex.data.repository.AuthRepository
import com.google.firebase.auth.*

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    var authState by mutableStateOf<FirebaseUser?>(repo.currentUser)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            authState = firebaseAuth.currentUser
        }
    }

    fun updateErrorMessage(message: String?) {
        errorMessage = message
    }

    suspend fun register(email: String, password: String) =
        repo.registerSuspend(email, password)   // implement suspend version in repo

    suspend fun login(email: String, password: String): Result<Unit> =
        repo.loginSuspend(email, password)

    fun logout() {
        repo.logout()
        authState = null
    }
}

