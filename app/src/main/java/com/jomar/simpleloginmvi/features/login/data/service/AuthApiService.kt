package com.jomar.simpleloginmvi.features.login.data.service

import com.jomar.simpleloginmvi.features.login.data.model.AuthResponse
import com.jomar.simpleloginmvi.features.login.data.model.LoginRequest
import com.jomar.simpleloginmvi.features.login.data.model.RegisterRequest

interface AuthApiService {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun logout(): AuthResponse
    suspend fun refreshToken(token: String): AuthResponse
}