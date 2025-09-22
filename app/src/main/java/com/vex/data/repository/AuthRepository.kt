package com.vex.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.*
import kotlinx.coroutines.tasks.await


class AuthRepository {

    private val auth = Firebase.auth

    suspend fun registerSuspend(email: String, password: String): Result<Unit> =
        runCatching {
            if (email.isBlank() || password.length < 6) {
                throw IllegalArgumentException("Please enter a valid email and a password >= 6 characters.")
            }
            auth.createUserWithEmailAndPassword(email, password).await()
            auth.currentUser?.sendEmailVerification()?.await()
        }


    suspend fun loginSuspend(email: String, password: String): Result<Unit> =
        runCatching {
            if (email.isBlank() || password.isBlank()) {
                throw IllegalArgumentException("Please provide email and password.")
            }
            auth.signInWithEmailAndPassword(email, password).await()
            val user = auth.currentUser
            if (user == null || !user.isEmailVerified) {
                throw IllegalStateException("Please verify your email address first.")
            }
        }


    fun logout() {
        auth.signOut()
    }

    val currentUser get() = auth.currentUser
}
