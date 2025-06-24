package com.jomar.simpleloginmvi.features.login.domain.usecase

import UserData
import UserRepository


class LoginUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(email: String, password: String): Result<UserData> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        return userRepository.login(email, password)
    }
}

class GetUserDataUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<UserData> {
        return userRepository.getUserData()
    }
}