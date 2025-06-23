package br.com.dmcard.contadigital.data_remote.mapper.auth

import br.com.dmcard.contadigital.data_remote.model.recoverpassword.RecoverPasswordFormBody
import br.com.dmcard.contadigital.data_remote.model.recoverpassword.ValidateChangePasswordTokenResponse
import br.com.dmcard.contadigital.domain.exception.MissingParamsException
import br.com.dmcard.contadigital.domain.model.recoverpassword.ChangePasswordToken
import br.com.dmcard.contadigital.domain.model.recoverpassword.RecoverPasswordForm

object RecoverPasswordMapper {
    fun fromDomain(data: RecoverPasswordForm) =
        RecoverPasswordFormBody(
            birthDate = data.birthDate ?: throw MissingParamsException(),
            taxId = data.cpf ?: throw MissingParamsException()
        )

    fun toDomain(data: ValidateChangePasswordTokenResponse) =
        ChangePasswordToken(
            passwordVoucher = data.passwordVoucher ?: ""
        )
}