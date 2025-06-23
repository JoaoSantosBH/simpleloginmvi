package com.jomar.simpleloginmvi.features.login.domain.usecase

import br.com.dmcard.contadigital.domain.exception.DataSourceException
import br.com.dmcard.contadigital.domain.exception.MissingParamsException
import br.com.dmcard.contadigital.domain.repository.AuthRepository
import br.com.dmcard.contadigital.domain.repository.DeviceRepository
import br.com.dmcard.contadigital.domain.repository.UserRepository
import br.com.dmcard.contadigital.domain.utils.constants.CHANGED_PASSWORD_CODE
import com.jomar.simpleloginmvi.features.login.domain.core.UseCase
import kotlinx.coroutines.CoroutineScope

class Login(
    scope: CoroutineScope,
    private val authRepository: AuthRepository,
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) : UseCase<Unit, Login.Param>(scope) {

    override fun run(params: Param?) = when (params) {
        null -> throw MissingParamsException()
        else -> try {
            deviceRepository.getAndroidId().flatMap {
                authRepository.login(
                    params.cpf, params.password, params.deviceToken, it
                )
            }
        } catch (throwable: DataSourceException) {
            throw handleWithLoginError(throwable)
        }
    }

    private fun handleWithLoginError(throwable: Exception): Throwable {
        if (throwable.message?.contains(CHANGED_PASSWORD_CODE) == true) {
            userRepository.removePassword()
            userRepository.saveAskBiometricAccess(true)
        }
        return throwable
    }

    data class Param(
        val cpf: String,
        val password: String,
        val deviceToken: String?
    )
}