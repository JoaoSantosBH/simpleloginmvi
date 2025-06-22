package com.jomar.simpleloginmvi.features.login.ui

sealed class LoginIntent {
    object LoadScreen : LoginIntent()
    data class EmailChanged(val email: String) : LoginIntent()
    data class PasswordChanged(val password: String) : LoginIntent()
    object LoginClicked : LoginIntent()
    object TogglePasswordVisibility : LoginIntent()
}