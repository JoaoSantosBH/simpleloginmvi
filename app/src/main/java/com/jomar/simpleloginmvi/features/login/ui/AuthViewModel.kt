package com.jomar.simpleloginmvi.features.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jomar.simpleloginmvi.features.login.data.model.AuthResponse
import com.jomar.simpleloginmvi.features.login.domain.usecase.GetCurrentUserUseCase
import com.jomar.simpleloginmvi.features.login.domain.usecase.LoginUseCase
import com.jomar.simpleloginmvi.features.login.domain.usecase.LogoutUseCase
import com.jomar.simpleloginmvi.features.login.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                is Result.Success<*> -> _authState.value = null
                is Result.Error -> _authState.value = result
                else -> {}
            }
        }
    }

    fun checkAuthState() {
        viewModelScope.launch {
            val result = getCurrentUserUseCase()
            when (result) {
                is Result.Success<*> -> {
                    if (result.data != null) {
                        // User is authenticated
                        _authState.value = Result.Success(
                            AuthResponse(
                                success = true,
                                message = "User authenticated",
                                token = result.data as String?
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

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    data class Loading(val isLoading: Boolean = true) : Result<Nothing>()
}