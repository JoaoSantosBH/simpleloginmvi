package br.com.dmcard.contadigital.data_remote.utils

import br.com.dmcard.contadigital.data.datasource.local.SessionLocalDataSource
import br.com.dmcard.contadigital.data_remote.model.generic.DmCardGenericResponse
import br.com.dmcard.contadigital.data_remote.model.generic.DmCardGenericTokenResponse
import br.com.dmcard.contadigital.data_remote.utils.constants.HEADER_API_TOKEN
import br.com.dmcard.contadigital.data_remote.utils.constants.HEADER_WSO2_TOKEN
import br.com.dmcard.contadigital.data_remote.utils.extensions.fromJson
import br.com.dmcard.contadigital.data_remote.utils.extensions.toJson
import br.com.dmcard.contadigital.domain.utils.extensions.formatToDateOrNull
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class AuthenticationInterceptor(
    private val sessionLocalStore: SessionLocalDataSource,
    private val hasXAuth: Boolean = true
) : Interceptor {

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = original.newBuilder().cacheControl(CacheControl.FORCE_NETWORK)

        addingHeaders(request)

        val response = chain.proceed(request.build())

        return tryToSaveDmCardToken(response)
    }

    @Synchronized
    fun addingHeaders(builder: Request.Builder) {
        val wso2Token = sessionLocalStore.getWso2Token()
        val apiToken = sessionLocalStore.getDMCardToken()

        if (wso2Token != null) {
            builder.header(HEADER_WSO2_TOKEN, "Bearer $wso2Token")
        }

        if (apiToken != null && hasXAuth) {
            builder.header(HEADER_API_TOKEN, "Bearer $apiToken")
        }
        builder.header("accept", "application/json")
    }

    fun tryToSaveDmCardToken(response: Response): Response {
        var newResponse = response
        val buffer = try {
            response.body?.bytes()?.clone()?.let {
                newResponse = response.newBuilder().body(it.toResponseBody()).build()
                String(it)
            }
        } catch (e: OutOfMemoryError) {
            null
        }

        buffer?.fromJson<DmCardGenericTokenResponse>()?.run {
            if (!token.isNullOrBlank()) sessionLocalStore.saveDMCardToken(token)
        }

        buffer?.fromJson<DmCardGenericResponse<Any>>()?.run {
            @Suppress("SENSELESS_COMPARISON")
            if (messageList == null || notice == null) return@run
            this.date = try {
                response.headers["Date"]?.formatToDateOrNull() ?: this.date
            } catch (ex: Exception) {
                this.date
            }
            newResponse = response.newBuilder().body(this.toJson().toResponseBody()).build()

        }

        return newResponse
    }

}
