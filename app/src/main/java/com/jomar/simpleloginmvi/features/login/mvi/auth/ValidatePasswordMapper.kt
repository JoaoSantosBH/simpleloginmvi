package br.com.dmcard.contadigital.data_remote.mapper.auth

import br.com.dmcard.contadigital.data_remote.model.recoverpassword.ValidateChangePasswordTokenResponse
import br.com.dmcard.contadigital.domain.model.recoverpassword.ChangePasswordToken

object ValidatePasswordMapper {

    fun toDomain(data: ValidateChangePasswordTokenResponse) =
        ChangePasswordToken(
            passwordVoucher = data.passwordVoucher ?: ""
        )
}