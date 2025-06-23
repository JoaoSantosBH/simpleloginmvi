package com.jomar.simpleloginmvi.features.login.domain.model

data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val profileImageUrl: String? = null,
    val createdAt: String
)