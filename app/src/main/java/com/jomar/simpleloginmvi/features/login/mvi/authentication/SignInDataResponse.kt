package br.com.dmcard.contadigital.data_remote.model.authentication

import com.google.gson.annotations.SerializedName

data class SignInDataResponse(
    @SerializedName("account") val accountResponse: SignInAccountResponse? = null,
    @SerializedName("token") val token: String? = null
)