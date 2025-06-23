package com.jomar.simpleloginmvi.features.login.data

sealed class AuthException(message: String) : Exception(message) {
    object NetworkError : AuthException("Network connection failed")
    object InvalidCredentials : AuthException("Invalid email or password")
    object UserAlreadyExists : AuthException("User already exists with this email")
    object WeakPassword : AuthException("Password is too weak")
    object InvalidEmail : AuthException("Invalid email format")
    data class ServerError(val code: Int, val serverMessage: String) : AuthException(serverMessage)
    object UnknownError : AuthException("An unknown error occurred")
}