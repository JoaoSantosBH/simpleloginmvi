package br.com.dmcard.contadigital.data_remote.service

import br.com.dmcard.contadigital.data_remote.model.authentication.SignInDataResponse
import br.com.dmcard.contadigital.data_remote.model.authentication.SignInRequest
import br.com.dmcard.contadigital.data_remote.model.generic.DmCardGenericResponse
import br.com.dmcard.contadigital.data_remote.model.recoverpassword.*
import br.com.dmcard.contadigital.data_remote.model.singledevice.SingleDeviceFormRequest
import br.com.dmcard.contadigital.data_remote.model.singledevice.SingleDeviceTokenRequest
import br.com.dmcard.contadigital.data_remote.service.AuthConstants.CHANGE_DEVICE
import br.com.dmcard.contadigital.data_remote.service.AuthConstants.CHANGE_DEVICE_TOKEN
import br.com.dmcard.contadigital.data_remote.service.AuthConstants.CHANGE_PASSWORD
import br.com.dmcard.contadigital.data_remote.service.AuthConstants.GET_ATTEMPTS_RECOVER_PASSWORD
import br.com.dmcard.contadigital.data_remote.service.AuthConstants.POST_AUTH_TOKEN
import br.com.dmcard.contadigital.data_remote.service.AuthConstants.POST_CHANGE_PASSWORD_TOKEN
import br.com.dmcard.contadigital.data_remote.service.AuthConstants.QUERY_CPF
import br.com.dmcard.contadigital.data_remote.service.AuthConstants.VALIDATE_CHANGE_PASSWORD_TOKEN
import br.com.dmcard.contadigital.data_remote.service.AuthConstants.VALIDATE_PASSWORD
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthWebService {

    @POST(POST_AUTH_TOKEN)
    suspend fun login(@Body signInRequest: SignInRequest): DmCardGenericResponse<SignInDataResponse>

    @POST(CHANGE_PASSWORD)
    suspend fun changePassword(
        @Body changePasswordBody: ChangePasswordBody
    ): DmCardGenericResponse<Unit>

    @POST(POST_CHANGE_PASSWORD_TOKEN)
    suspend fun sendRecoverPasswordForm(
        @Body recoverPasswordFormBody: RecoverPasswordFormBody
    ): DmCardGenericResponse<Unit>

    @POST(VALIDATE_CHANGE_PASSWORD_TOKEN)
    suspend fun validateRecoverPasswordCode(
        @Body validateChangePasswordTokenBody: ValidateChangePasswordTokenBody
    ): DmCardGenericResponse<ValidateChangePasswordTokenResponse>

    @GET(GET_ATTEMPTS_RECOVER_PASSWORD)
    suspend fun getAttemptsToRecoverPassword(
        @Query(QUERY_CPF) cpf: String
    ): DmCardGenericResponse<AttemptResponse>

    @POST(VALIDATE_PASSWORD)
    suspend fun validatePassword(
        @Body validatePassword: ValidatePasswordBody
    ): DmCardGenericResponse<ValidateChangePasswordTokenResponse>

    @POST(CHANGE_DEVICE_TOKEN)
    suspend fun sendSingleDeviceForm(
        @Body validatePassword: SingleDeviceFormRequest
    ): DmCardGenericResponse<Unit>

    @POST(CHANGE_DEVICE)
    suspend fun singleDeviceValidateSmsCode(
        @Body validatePassword: SingleDeviceTokenRequest
    ): DmCardGenericResponse<Unit>

}

object AuthConstants {
    const val POST_AUTH_TOKEN = "auth_token"
    const val POST_CHANGE_PASSWORD_TOKEN = "change_password_token"
    const val GET_ATTEMPTS_RECOVER_PASSWORD = "attempts"
    const val VALIDATE_CHANGE_PASSWORD_TOKEN = "validate_change_password_token"
    const val CHANGE_PASSWORD = "change_password"
    const val VALIDATE_PASSWORD = "validate_password"
    const val CHANGE_DEVICE_TOKEN = "change_device_token"
    const val CHANGE_DEVICE = "change_device"

    const val QUERY_CPF = "tax_id"
}