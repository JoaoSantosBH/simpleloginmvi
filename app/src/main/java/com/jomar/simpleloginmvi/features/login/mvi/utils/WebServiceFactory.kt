package br.com.dmcard.contadigital.data_remote.utils

import br.com.dmcard.contadigital.data.datasource.local.SessionLocalDataSource
import br.com.dmcard.contadigital.data_remote.certificate.DmCardCertificate
import br.com.dmcard.contadigital.data_remote.utils.constants.BASE_URL_APP
import br.com.dmcard.contadigital.data_remote.utils.constants.BASE_URL_GATEWAY
import br.com.dmcard.contadigital.data_remote.utils.constants.PROPOSAL_BASE_URL
import br.com.dmcard.contadigital.data_remote.utils.constants.TIMEOUT_DURATION_SECONDS
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.CertificatePinner
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.lang.reflect.Type
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

object WebServiceFactory {

    inline fun <reified T> createWebService(
        wasDebugVersion: Boolean,
        okHttpClient: OkHttpClient,
        needBaseUrl: Boolean = true,
        url: String = PROPOSAL_BASE_URL,
        acceptLenient: Boolean = false
    ): T {

        val gsonFactory = if (acceptLenient) {
            val gson = GsonBuilder().setLenient().create()
            GsonConverterFactory.create(gson)
        } else {
            GsonConverterFactory.create()
        }

        val baseUrl = when (needBaseUrl) {
            true -> getBaseUrl(wasDebugVersion).plus(url)
            false -> url
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(UnitConverterFactory)
            .addConverterFactory(gsonFactory)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
        return retrofit.create()
    }

    fun getBaseUrl(wasDebugVersion: Boolean) = when (wasDebugVersion) {
        true -> BASE_URL_GATEWAY
        false -> BASE_URL_APP
    }

    fun provideOkHttpClient(
        sessionLocalStore: SessionLocalDataSource,
        wasDebugVersion: Boolean,
        certificate: DmCardCertificate? = null,
        hasXAuth: Boolean = true
    ): OkHttpClient = OkHttpClient.Builder()
        .dispatcher(dispatcher())
        .addInterceptor(AuthenticationInterceptor(sessionLocalStore, hasXAuth))
        .httpLoggingInterceptor(wasDebugVersion)
        .connectTimeout(TIMEOUT_DURATION_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_DURATION_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_DURATION_SECONDS, TimeUnit.SECONDS)
        .apply {
            if (certificate == null) return@apply
//TODO pinagem totalmente desabilitada a pedido do Jailson
            if (!wasDebugVersion) {
                certificatePinner(certificatePinner())
                initSSL(certificate)?.let { sslSocketFactory ->
                    systemDefaultTrustManager()?.let { x509TrustManager ->
                        sslSocketFactory(sslSocketFactory, x509TrustManager)
                    }
                }
            }
        }
        .build()

    private fun OkHttpClient.Builder.httpLoggingInterceptor(wasDebugVersion: Boolean) =
        when (wasDebugVersion) {
            true -> {
                val interceptor = HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
                addInterceptor(interceptor)
            }

            else -> this
        }

    fun defaultBuilder(wasDebugVersion: Boolean) = OkHttpClient.Builder()
        .cache(null)
        .httpLoggingInterceptor(wasDebugVersion)
        .connectTimeout(TIMEOUT_DURATION_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_DURATION_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_DURATION_SECONDS, TimeUnit.SECONDS)
        .build()

    private fun dispatcher() = Dispatcher().run {
        maxRequests = 1
        maxRequestsPerHost = 1
        this
    }

    private fun certificatePinner() = CertificatePinner.Builder()
        .add("gateway.dmcardapi.com.br", "sha256/ExUEQXo3cgi9SxG+P77QYUIAG5h+Uv0USIaI1CdqWB0=")
        .add("app.dmcardapi.com.br", "sha256/k7Ik+1wrlAJESOwqxa0CHYFLPlizyd/tOI9lEFw1k8Q=")
        .add("gateway.api.cloud.wso2.com", "sha256/3M7EFYIF9skgpz89nnNqcoZHyXNGn2Xz2k3+pdUkyfY=")
        .build()

    private fun initSSL(certificate: DmCardCertificate): SSLSocketFactory? = try {
        val sslContext = createCertificate(certificate)
        sslContext?.socketFactory
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun systemDefaultTrustManager(): X509TrustManager? = try {
        val trustManagerFactory =
            TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            ).also {
                it.init(null as KeyStore?)
            }
        val trustManagers: Array<TrustManager> = trustManagerFactory.trustManagers
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            ("Unexpected default trust managers: ${trustManagers.contentToString()}")
        }

        trustManagers[0] as X509TrustManager
    } catch (e: GeneralSecurityException) {
        null
    }

    private fun createCertificate(certificate: DmCardCertificate): SSLContext? {
        try {
            // creating a KeyStore containing our trusted CAs
            val keyStore: KeyStore = KeyStore.getInstance(
                KeyStore.getDefaultType()
            ).also {
                it.load(null, null)
                it.setCertificateEntry(
                    "ca",
                    CertificateFactory.getInstance("X.509").generateCertificate(certificate)
                )
            }

            // creating a TrustManager that trusts the CAs
            val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            ).also {
                it.init(keyStore)
            }

            // returning an SSLSocketFactory that uses TrustManager
            return SSLContext.getInstance("TLS").let {
                it.init(null, tmf.trustManagers, null)
                it
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    object UnitConverterFactory : Converter.Factory() {
        override fun responseBodyConverter(
            type: Type, annotations: Array<out Annotation>,
            retrofit: Retrofit
        ): Converter<ResponseBody, *>? {
            return if (type == Unit::class.java) UnitConverter else null
        }

        private object UnitConverter : Converter<ResponseBody, Unit> {
            override fun convert(value: ResponseBody) {
                value.close()
            }
        }
    }

}


