package com.jomar.simpleloginmvi.features.login.domain.usecase

import com.jomar.simpleloginmvi.features.login.data.Result
import com.jomar.simpleloginmvi.features.login.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}