package com.jomar.simpleloginmvi.features.login.data.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)