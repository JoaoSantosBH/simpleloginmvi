package br.com.dmcard.contadigital.data.datasource.remote

import br.com.dmcard.contadigital.data.models.DmLoginDataRequest
import br.com.dmcard.contadigital.data.models.SignInResponse
import br.com.dmcard.contadigital.domain.model.recoverpassword.ChangePasswordToken
import br.com.dmcard.contadigital.domain.model.recoverpassword.RecoverPasswordForm
import kotlinx.coroutines.flow.Flow

interface AuthRemoteDataSource {

    fun login(loginData: DmLoginDataRequest): Flow<SignInResponse>
    fun changePassword(password: String, passwordVoucher: String): Flow<Unit>
    fun sendRecoverPasswordForm(form: RecoverPasswordForm): Flow<Unit>
    fun getAttemptsToRecoverPassword(cpf: String): Flow<Int>
    fun recoverPasswordValidateSmsCode(taxId: String, code: String): Flow<ChangePasswordToken>
    fun validatePassword(password: String): Flow<ChangePasswordToken>
    fun sendSingleDeviceForm(taxId: String): Flow<Unit>
    fun singleDeviceValidateSmsCode(taxId: String, deviceId: String, code: String): Flow<Unit>

}