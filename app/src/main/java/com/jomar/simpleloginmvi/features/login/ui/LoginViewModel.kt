package com.jomar.simpleloginmvi.features.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class LoginViewModel : ViewModel() {

    private val _viewState = MutableStateFlow(LoginViewState())
    val viewState: StateFlow<LoginViewState> = _viewState.asStateFlow()

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.LoadScreen -> loadScreen()
            is LoginIntent.EmailChanged -> updateEmail(intent.email)
            is LoginIntent.PasswordChanged -> updatePassword(intent.password)
            is LoginIntent.LoginClicked -> performLogin()
            is LoginIntent.TogglePasswordVisibility -> togglePasswordVisibility()
        }
    }

    private fun loadScreen() {
        _viewState.value = LoginViewState()
    }

    private fun updateEmail(email: String) {
        _viewState.value = _viewState.value.copy(
            email = email,
            emailError = null,
            loginError = null
        )
    }

    private fun updatePassword(password: String) {
        _viewState.value = _viewState.value.copy(
            password = password,
            passwordError = null,
            loginError = null
        )
    }

    private fun togglePasswordVisibility() {
        _viewState.value = _viewState.value.copy(
            isPasswordVisible = !_viewState.value.isPasswordVisible
        )
    }

    private fun performLogin() {
        val currentState = _viewState.value

        // Validate inputs
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)

        if (emailError != null || passwordError != null) {
            _viewState.value = currentState.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        // Show loading state
        _viewState.value = currentState.copy(
            isLoading = true,
            emailError = null,
            passwordError = null,
            loginError = null
        )

        // Simulate API call
        viewModelScope.launch {
            try {
                delay(2000) // Simulate network delay

                // Simulate login logic
                val isSuccess = simulateLogin(currentState.email, currentState.password)

                _viewState.value = if (isSuccess) {
                    currentState.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    )
                } else {
                    currentState.copy(
                        isLoading = false,
                        loginError = "Invalid email or password"
                    )
                }
            } catch (e: Exception) {
                _viewState.value = currentState.copy(
                    isLoading = false,
                    loginError = "Network error. Please try again."
                )
            }
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    private fun simulateLogin(email: String, password: String): Boolean {
        // Simulate API call - returns true for demo@example.com / password123
        return email == "demo@example.com" && password == "password123"
    }
}