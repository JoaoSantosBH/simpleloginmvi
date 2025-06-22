package com.jomar.simpleloginmvi.features.login.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data Models
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: UserData? = null,
    val token: String? = null
)

data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val profileImageUrl: String? = null,
    val createdAt: String
)

// Result wrapper for handling success/error states
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    data class Loading(val isLoading: Boolean = true) : Result<Nothing>()
}

// Custom exceptions
sealed class AuthException(message: String) : Exception(message) {
    object NetworkError : AuthException("Network connection failed")
    object InvalidCredentials : AuthException("Invalid email or password")
    object UserAlreadyExists : AuthException("User already exists with this email")
    object WeakPassword : AuthException("Password is too weak")
    object InvalidEmail : AuthException("Invalid email format")
    data class ServerError(val code: Int, val serverMessage: String) : AuthException(serverMessage)
    object UnknownError : AuthException("An unknown error occurred")
}

// Repository Interface (Domain Layer)
interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): Result<String>
}

// API Service Interface
interface AuthApiService {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun logout(): AuthResponse
    suspend fun refreshToken(token: String): AuthResponse
}

// Token Manager Interface
interface TokenManager {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
    suspend fun saveRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
}

// Repository Implementation (Data Layer)
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
                    else -> AuthException.ServerError(exception.responseCode(), exception.reason ?: "Server error")
                }
            }
            else -> AuthException.UnknownError
        }
    }
}

// Login Use Case
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

// Register Use Case
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

// Logout Use Case
class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}

// Get Current User Use Case (for checking auth state)
class GetCurrentUserUseCase(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<String?> {
        return try {
            val token = tokenManager.getToken()
            if (token != null) {
                // Optionally validate token with server
                Result.Success(token)
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(AuthException.UnknownError)
        }
    }
}

// Usage Example in ViewModel
class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<Result<AuthResponse>?>(null)
    val authState: StateFlow<Result<AuthResponse>?> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Result.Loading()
            _authState.value = loginUseCase(email, password)
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _authState.value = Result.Loading()
            _authState.value = registerUseCase(name, email, password, confirmPassword)
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = Result.Loading()
            val result = logoutUseCase()
            when (result) {
                is Result.Success -> _authState.value = null
                is Result.Error -> _authState.value = result
                else -> {}
            }
        }
    }

    fun checkAuthState() {
        viewModelScope.launch {
            val result = getCurrentUserUseCase()
            when (result) {
                is Result.Success -> {
                    if (result.data != null) {
                        // User is authenticated
                        _authState.value = Result.Success(
                            AuthResponse(
                                success = true,
                                message = "User authenticated",
                                token = result.data
                            )
                        )
                    }
                }
                is Result.Error -> _authState.value = result
                else -> {}
            }
        }
    }
}