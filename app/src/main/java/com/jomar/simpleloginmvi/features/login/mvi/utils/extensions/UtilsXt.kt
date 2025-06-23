package br.com.dmcard.contadigital.data_remote.utils.extensions

import br.com.dmcard.contadigital.data_remote.model.proposal.CreateProposalRequest
import br.com.dmcard.contadigital.data_remote.utils.constants.DEVICE_TYPE
import br.com.dmcard.contadigital.data_remote.utils.constants.SHA_512
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

inline fun <reified T> String.fromJson(): T? = try {
    Gson().getAdapter(TypeToken.get(T::class.java)).fromJson(this)
} catch (e: Exception) {
    null
}

fun String.toRequestBody(): RequestBody =
    toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

fun File.getMultipartBodyJpg(name: String): MultipartBody.Part {
    val requestFile = this.asRequestBody("image/jpeg".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(name, this.name, requestFile)
}

fun String.trimQuerysFromUrl() = replace("""\?.*""".toRegex(), "")

fun HttpException.is4xxError() = code() in 400..499

inline fun <reified T> JsonElement.fromJson(): T? = try {
    Gson().getAdapter(TypeToken.get(T::class.java)).fromJsonTree(this)
} catch (e: Exception) {
    null
}

fun Any.toJson(): String = Gson().toJson(this)

val JsonElement.asJsonObjectOrNull
    get() = try {
        asJsonObject
    } catch (e: java.lang.Exception) {
        null
    }

val JsonElement.asJsonPrimitiveOrNull
    get() = try {
        asJsonPrimitive
    } catch (e: java.lang.Exception) {
        null
    }

val JsonElement.asNumberOrNull
    get() = try {
        asNumber
    } catch (e: Exception) {
        null
    }

val JsonElement.asBooleanOrFalse
    get() = try {
        asBoolean
    } catch (e: Exception) {
        null
    }

fun Double.truncateDecimal() = toInt().toDouble()

fun String.formatLimitTimeFirstParameter() =
    if (this.substring(0, 1).contains("0")) this.substring(1) else this

fun createHash512(cpf: String, id: String): CreateProposalRequest {
    val strHash = cpf + id + DEVICE_TYPE
    val md = MessageDigest.getInstance(SHA_512)
    val messageDigest = md.digest(strHash.toByteArray())
    val sigNum = BigInteger(1, messageDigest)
    var hashText = sigNum.toString(16)
    while (hashText.length < 128) {
        hashText = "0$hashText"
    }
    return CreateProposalRequest(cpf, id, hashText)
}