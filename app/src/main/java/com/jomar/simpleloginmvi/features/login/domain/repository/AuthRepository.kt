package com.jomar.simpleloginmvi.features.login.domain.repository

import com.jomar.simpleloginmvi.features.login.data.model.AuthResponse
import com.jomar.simpleloginmvi.features.login.data.model.LoginRequest
import com.jomar.simpleloginmvi.features.login.data.model.RegisterRequest

interface AuthRepository {

    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): Result<String>

}