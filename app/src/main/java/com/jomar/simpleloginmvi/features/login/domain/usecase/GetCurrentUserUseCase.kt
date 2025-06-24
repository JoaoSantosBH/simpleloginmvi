package com.jomar.simpleloginmvi.features.login.domain.usecase

import com.jomar.simpleloginmvi.features.login.data.AuthException
import com.jomar.simpleloginmvi.features.login.domain.repository.AuthRepository
import com.jomar.simpleloginmvi.features.login.mvi.TokenManager

class GetCurrentUserUseCase(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<String?> {
        return try {
            val token = tokenManager.getToken()
            if (token != null) {
                // Optionally validate token with server
                Result.success(token)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(AuthException.UnknownError)
        }
    }
}