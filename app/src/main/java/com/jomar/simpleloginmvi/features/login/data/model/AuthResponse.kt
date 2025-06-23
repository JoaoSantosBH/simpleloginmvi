package com.jomar.simpleloginmvi.features.login.data.model

import com.jomar.simpleloginmvi.features.login.domain.model.UserData

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: UserData? = null,
    val token: String? = null
)