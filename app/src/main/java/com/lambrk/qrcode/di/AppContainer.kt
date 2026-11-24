package com.lambrk.qrcode.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.lambrk.qrcode.data.login.ExternalLoginChecker
import com.lambrk.qrcode.data.login.PlaceholderLoginChecker
import com.lambrk.qrcode.data.network.NetworkMonitor
import com.lambrk.qrcode.data.remote.ApiConfig
import com.lambrk.qrcode.data.remote.VerifyApi
import com.lambrk.qrcode.data.repository.VerificationRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Hand-rolled dependency container (kept simple; swap for Hilt/Koin if the app grows). */
class AppContainer(context: Context) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .apply {
            // Chucker (API inspector + notification) only against the development URL.
            if (ApiConfig.isDevelopment) addInterceptor(ChuckerInterceptor.Builder(context).build())
        }
        .build()

    val networkMonitor = NetworkMonitor(context, ApiConfig.baseUrl.takeIf { it.isNotBlank() })

    /** Swap for the real implementation once the other app integration exists. */
    val loginChecker: ExternalLoginChecker = PlaceholderLoginChecker()

    val verificationRepository = VerificationRepository(
        ApiConfig.baseUrl.takeIf { it.isNotBlank() }?.let { baseUrl ->
            Retrofit.Builder()
                .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VerifyApi::class.java)
        }
    )
}
