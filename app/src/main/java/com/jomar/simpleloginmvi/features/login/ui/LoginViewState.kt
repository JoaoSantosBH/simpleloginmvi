package com.jomar.simpleloginmvi.features.login.ui

data class LoginViewState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginError: String? = null,
    val isLoginSuccessful: Boolean = false
)