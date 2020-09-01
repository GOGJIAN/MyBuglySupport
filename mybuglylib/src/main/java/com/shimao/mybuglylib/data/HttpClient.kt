package com.shimao.mybuglylib.data

import android.annotation.SuppressLint
import android.text.TextUtils
import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


/**
 * @author jian
 *
 */
object HttpClient {
    private var httpClient : OkHttpClient ?= null
    private const val DEFAULT_CONNECT_TIMEOUT = 15
    private const val DEFAULT_READ_TIMEOUT = 15
    private const val DEFAULT_WRITE_TIMEOUT = 15
    private val DEFAULT_TIME_UNIT = TimeUnit.SECONDS
    fun getHttpClient(isDebug: Boolean):OkHttpClient{
        if(httpClient == null){
            synchronized(HttpClient::class.java){
                if (httpClient == null){
                    createHttpClient(isDebug)
                }
            }
        }
        return httpClient!!
    }

    private fun createHttpClient(isDebug: Boolean) {
        val tm = object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(java.security.cert.CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(java.security.cert.CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        }
        var sslSocketFactory: SSLSocketFactory? = null
        try {
            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf<TrustManager>(tm), java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            sslSocketFactory = sslContext.socketFactory
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val host = System.getProperty("http.proxyHost")
        val port = System.getProperty("http.proxyPort")
        var portInt = 0
        if (port != null) {
            portInt = Integer.parseInt(port)
        }
        val builder = OkHttpClient().newBuilder()
            .readTimeout(DEFAULT_READ_TIMEOUT.toLong(), DEFAULT_TIME_UNIT)
            .writeTimeout(DEFAULT_WRITE_TIMEOUT.toLong(), DEFAULT_TIME_UNIT)
            .connectTimeout(DEFAULT_CONNECT_TIMEOUT.toLong(), DEFAULT_TIME_UNIT)
            .addInterceptor(LogInterceptor())
        if(isDebug){
            if (!TextUtils.isEmpty(host) && portInt != 0) {
                builder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(host, portInt)))
                    .hostnameVerifier(HostnameVerifier { _, _ -> true })
                    .sslSocketFactory(sslSocketFactory!!, tm)
            }
        }
        httpClient = builder.build()
    }
}
