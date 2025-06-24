package com.jomar.simpleloginmvi.features.login.domain.usecase

import com.jomar.simpleloginmvi.features.login.data.AuthException
import com.jomar.simpleloginmvi.features.login.data.Result
import com.jomar.simpleloginmvi.features.login.data.model.AuthResponse
import com.jomar.simpleloginmvi.features.login.data.model.RegisterRequest
import com.jomar.simpleloginmvi.features.login.domain.repository.AuthRepository


class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<AuthResponse> {
        // Validate input
        val validationError = validateRegisterInput(name, email, password, confirmPassword)
        if (validationError != null) {
            return Result.Error(validationError)
        }

        // Create request
        val request = RegisterRequest(
            name = name.trim(),
            email = email.trim().lowercase(),
            password = password,
            confirmPassword = confirmPassword
        )

        // Execute registration
        return authRepository.register(request)
    }

    private fun validateRegisterInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AuthException? {
        return when {
            name.isBlank() || name.length < 2 ->
                AuthException.ServerError(400, "Name must be at least 2 characters")

            email.isBlank() -> AuthException.InvalidEmail
            !isValidEmail(email) -> AuthException.InvalidEmail
            password.isBlank() -> AuthException.WeakPassword
            password.length < 6 -> AuthException.WeakPassword
            password != confirmPassword ->
                AuthException.ServerError(400, "Passwords do not match")

            !isStrongPassword(password) -> AuthException.WeakPassword
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isStrongPassword(password: String): Boolean {
        return password.length >= 6 &&
                password.any { it.isDigit() } &&
                password.any { it.isLetter() }
    }
}