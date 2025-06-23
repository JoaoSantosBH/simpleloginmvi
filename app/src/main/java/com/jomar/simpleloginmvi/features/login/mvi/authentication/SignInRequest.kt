package br.com.dmcard.contadigital.data_remote.model.authentication

import com.google.gson.annotations.SerializedName

data class SignInRequest(
    @SerializedName("tax_id") val cpf: String,
    @SerializedName("password") val password: String,
    @SerializedName("device_id") val deviceId: String?,
    @SerializedName("device_token") val deviceToken: String?,
    @SerializedName("device_type") val deviceType: String = "android"
)