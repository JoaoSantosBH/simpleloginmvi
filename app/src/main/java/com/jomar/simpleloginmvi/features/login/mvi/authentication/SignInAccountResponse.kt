package br.com.dmcard.contadigital.data_remote.model.authentication

import com.google.gson.annotations.SerializedName

data class SignInAccountResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("agency") val agency: String? = null,
    @SerializedName("account") val account: String? = null,
    @SerializedName("tax_id") val cpf: String? = null,
    @SerializedName("preferred_name") val preferredName: String? = null
)