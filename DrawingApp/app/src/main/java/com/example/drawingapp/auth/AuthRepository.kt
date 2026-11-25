package com.example.drawingapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Repository for managing Firebase Email + Password User Authentication operations.
 */
class AuthRepository(
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
) {
    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    val currentUserEmail: String?
        get() = auth?.currentUser?.email

    val currentUserId: String?
        get() = auth?.currentUser?.uid

    val isLoggedIn: Boolean
        get() = auth?.currentUser != null

    suspend fun signUp(email: String, pass: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { continuation ->
            val firebaseAuth = auth
            if (firebaseAuth == null) {
                continuation.resume(Result.failure(Exception("FirebaseAuth is not initialized.")))
                return@suspendCancellableCoroutine
            }
            firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        continuation.resume(Result.success(user))
                    } else {
                        continuation.resume(Result.failure(Exception("User registration returned null user.")))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }

    suspend fun signIn(email: String, pass: String): Result<FirebaseUser> =
        suspendCancellableCoroutine { continuation ->
            val firebaseAuth = auth
            if (firebaseAuth == null) {
                continuation.resume(Result.failure(Exception("FirebaseAuth is not initialized.")))
                return@suspendCancellableCoroutine
            }
            firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        continuation.resume(Result.success(user))
                    } else {
                        continuation.resume(Result.failure(Exception("Sign in returned null user.")))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }

    fun signOut() {
        auth?.signOut()
    }
}
