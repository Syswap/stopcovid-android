/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Authors
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Created by Lunabee Studio / Date - 2020/13/05 - for the STOP-COVID project
 */

package com.lunabeestudio.stopcovid.coreui.network

import android.content.Context
import android.os.Build
import com.lunabeestudio.stopcovid.coreui.BuildConfig
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.tls.HandshakeCertificates
import timber.log.Timber
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

object OkHttpClient {

    fun getDefaultOKHttpClient(context: Context, url: String, certificateSHA256: String): OkHttpClient {
        val requireTls12 = ConnectionSpec.Builder(ConnectionSpec.RESTRICTED_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .build()
        return OkHttpClient.Builder().apply {
            if (!BuildConfig.DEBUG) {
                connectionSpecs(listOf(requireTls12))
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                certificatePinner(CertificatePinner.Builder()
                    .add(url.toHttpUrl().host, certificateSHA256)
                    .build())
                val certificates: HandshakeCertificates = HandshakeCertificates.Builder()
                    .addTrustedCertificate(certificateFromString(context, "app_stopcovid_gouv_fr"))
                    .build()
                sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager)
            }
            addInterceptor(getDefaultHeaderInterceptor())
            addInterceptor(getLogInterceptor())
            callTimeout(1L, TimeUnit.MINUTES)
            connectTimeout(1L, TimeUnit.MINUTES)
            readTimeout(1L, TimeUnit.MINUTES)
            writeTimeout(1L, TimeUnit.MINUTES)
        }.build()
    }

    private fun certificateFromString(context: Context, fileName: String): X509Certificate {
        return CertificateFactory.getInstance("X.509").generateCertificate(
            context.resources.openRawResource(
                context.resources.getIdentifier(fileName,
                    "raw", context.packageName)
            )) as X509Certificate
    }

    private fun getDefaultHeaderInterceptor(): Interceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
                .newBuilder().apply {
                    addHeader("Accept", "application/json")
                    addHeader("Content-Type", "application/json")
                }.build()
            return chain.proceed(request)
        }
    }

    private fun getLogInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            Timber.d(message)
        }
    }).apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }
}
