package com.jomar.simpleloginmvi.features.login.data

import com.jomar.simpleloginmvi.features.login.data.model.AuthResponse
import com.jomar.simpleloginmvi.features.login.data.model.LoginRequest
import com.jomar.simpleloginmvi.features.login.data.model.RegisterRequest
import com.jomar.simpleloginmvi.features.login.data.service.AuthApiService
import com.jomar.simpleloginmvi.features.login.domain.repository.AuthRepository
import com.jomar.simpleloginmvi.features.login.mvi.TokenManager

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(request)

            if (response.success && response.token != null) {
                tokenManager.saveToken(response.token)
                Result.Success(response)
            } else {
                Result.Error(AuthException.InvalidCredentials)
            }
        } catch (e: Exception) {
            Result.Error(handleApiException(e))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(request)

            if (response.success && response.token != null) {
                tokenManager.saveToken(response.token)
                Result.Success(response)
            } else {
                Result.Error(AuthException.ServerError(400, response.message))
            }
        } catch (e: Exception) {
            Result.Error(handleApiException(e))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            apiService.logout()
            tokenManager.clearToken()
            Result.Success(Unit)
        } catch (e: Exception) {
            // Clear token even if API call fails
            tokenManager.clearToken()
            Result.Success(Unit)
        }
    }

    override suspend fun refreshToken(): Result<String> {
        return try {
            val currentToken = tokenManager.getRefreshToken()
                ?: return Result.Error(AuthException.InvalidCredentials)

            val response = apiService.refreshToken(currentToken)

            if (response.success && response.token != null) {
                tokenManager.saveToken(response.token)
                Result.Success(response.token)
            } else {
                Result.Error(AuthException.InvalidCredentials)
            }
        } catch (e: Exception) {
            Result.Error(handleApiException(e))
        }
    }

    private fun handleApiException(exception: Exception): AuthException {
        return when (exception) {
            is java.net.UnknownHostException,
            is java.net.ConnectException -> AuthException.NetworkError

            is java.net.HttpRetryException -> {
                when (exception.responseCode()) {
                    401 -> AuthException.InvalidCredentials
                    409 -> AuthException.UserAlreadyExists
                    else -> AuthException.ServerError(
                        exception.responseCode(),
                        exception.reason ?: "Server error"
                    )
                }
            }

            else -> AuthException.UnknownError
        }
    }
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    data class Loading(val isLoading: Boolean = true) : Result<Nothing>()
}