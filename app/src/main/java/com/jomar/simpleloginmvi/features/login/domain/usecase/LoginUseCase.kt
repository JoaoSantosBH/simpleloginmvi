package com.jomar.simpleloginmvi.features.login.domain.usecase

import com.jomar.simpleloginmvi.features.login.data.AuthException
import com.jomar.simpleloginmvi.features.login.data.model.AuthResponse
import com.jomar.simpleloginmvi.features.login.data.model.LoginRequest
import com.jomar.simpleloginmvi.features.login.domain.repository.AuthRepository


class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthResponse> {
        // Validate input
        val validationError = validateLoginInput(email, password)
        if (validationError != null) {
            return Result.Error(validationError)
        }

        // Create request
        val request = LoginRequest(
            email = email.trim().lowercase(),
            password = password
        )

        // Execute login
        return authRepository.login(request)
    }

    private fun validateLoginInput(email: String, password: String): AuthException? {
        return when {
            email.isBlank() -> AuthException.InvalidEmail
            password.isBlank() -> AuthException.WeakPassword
            !isValidEmail(email) -> AuthException.InvalidEmail
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}