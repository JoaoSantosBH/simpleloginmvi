package br.com.dmcard.contadigital.data_remote.mapper.auth

import br.com.dmcard.contadigital.data.models.DmLoginDataRequest
import br.com.dmcard.contadigital.data.models.SignInResponse
import br.com.dmcard.contadigital.data_remote.model.authentication.SignInDataResponse
import br.com.dmcard.contadigital.data_remote.model.authentication.SignInRequest
import br.com.dmcard.contadigital.data_remote.utils.constants.AuthenticationTimeoutException
import br.com.dmcard.contadigital.data_remote.utils.constants.ErrorMessageEnum
import br.com.dmcard.contadigital.data_remote.utils.constants.RemoteDataSourceException
import br.com.dmcard.contadigital.data_remote.utils.constants.UnauthorizedException
import br.com.dmcard.contadigital.domain.model.useraccount.UserAccountInformation

object SignInMapper {
    fun fromData(domain: DmLoginDataRequest) = SignInRequest(
        cpf = domain.cpf,
        password = domain.password,
        deviceToken = domain.firebaseToken,
        deviceId = domain.deviceId
    )

    fun toData(data: SignInDataResponse) =
        SignInResponse(
            token = data.token!!,
            accountInfo = UserAccountInformation(
                userId = data.accountResponse!!.id!!,
                account = data.accountResponse.account!!,
                name = data.accountResponse.name!!,
                preferredName = data.accountResponse.preferredName!!,
                email = "",
                phone = "",
                agency = data.accountResponse.agency!!,
                cpf = data.accountResponse.cpf!!
            )
        )

    fun exceptionToData(e: Throwable) =
        when (e is UnauthorizedException || e is AuthenticationTimeoutException) {
            true -> RemoteDataSourceException(
                message = e.message ?: ErrorMessageEnum.INVALID_LOGIN_ERROR.value,
                code = "",
                messageList = listOf()
            )

            false -> e
        }
}