package com.example.drawingapp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val userEmail: String? = null,
    val userId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignUpMode: Boolean = false
)

/**
 * ViewModel handling UI state for User Authentication.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = authRepository.isLoggedIn,
            userEmail = authRepository.currentUserEmail,
            userId = authRepository.currentUserId
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateAuthState() {
        _uiState.value = _uiState.value.copy(
            isLoggedIn = authRepository.isLoggedIn,
            userEmail = authRepository.currentUserEmail,
            userId = authRepository.currentUserId
        )
    }

    fun toggleSignUpMode() {
        _uiState.value = _uiState.value.copy(
            isSignUpMode = !_uiState.value.isSignUpMode,
            errorMessage = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun authenticate(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val isSignUp = _uiState.value.isSignUpMode
            val result = if (isSignUp) {
                authRepository.signUp(email, pass)
            } else {
                authRepository.signIn(email, pass)
            }

            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    userEmail = user.email,
                    userId = user.uid,
                    isLoading = false,
                    errorMessage = null
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.localizedMessage ?: "Authentication failed."
                )
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        updateAuthState()
    }
}
