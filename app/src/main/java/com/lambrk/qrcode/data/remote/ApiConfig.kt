package com.lambrk.qrcode.data.remote

import com.lambrk.qrcode.BuildConfig

/**
 * Backend URLs. Both are plain http (cleartext is allowed in the manifest).
 * Debug builds use [DEV_BASE_URL], release builds use [PROD_BASE_URL].
 * Must end with "/". Set a URL to "" to run in offline-only mode.
 *
 * Dev default 10.0.2.2 = your computer's localhost when running on the Android emulator;
 * on a real phone use your computer's LAN IP, e.g. "http://192.168.1.10:8080/".
 */
object ApiConfig {
    private const val DEV_BASE_URL = "http://10.0.2.2:8080/"
    private const val PROD_BASE_URL = "http://api.example.com/" // TODO: replace with the real production URL

    /** true = development (debug build): dev URL, Chucker enabled. false = production. */
    val isDevelopment: Boolean = BuildConfig.DEBUG

    val baseUrl: String = if (isDevelopment) DEV_BASE_URL else PROD_BASE_URL
}
