package com.jomar.simpleloginmvi.features.login.mvi

interface TokenManager {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
    suspend fun saveRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
}