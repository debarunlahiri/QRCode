package com.lambrk.qrcode.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.lambrk.qrcode.domain.model.ConnectionIssue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Watches Wi-Fi (or Ethernet) and checks that the backend [serverUrl] answers.
 * Any HTTP answer, even 404, means the server is reachable; only a failure to connect counts as an error.
 * A null [serverUrl] disables the server check.
 */
class NetworkMonitor(context: Context, private val serverUrl: String?) {
    private val manager = context.getSystemService(ConnectivityManager::class.java)

    // Plain client on purpose: health checks should not show up in Chucker.
    private val healthClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    private val retryTick = MutableStateFlow(0)

    fun isWifiConnected(): Boolean = manager.getNetworkCapabilities(manager.activeNetwork).isWifi()

    val wifiConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(caps.isWifi())
            }

            override fun onLost(network: Network) {
                trySend(isWifiConnected())
            }
        }
        trySend(isWifiConnected())
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    /**
     * Current connection problem, or null when all is fine (or while a check is running).
     * Re-evaluated whenever Wi-Fi changes or [retry] is called.
     */
    val issue: Flow<ConnectionIssue?> = combine(wifiConnected, retryTick) { wifi, _ -> wifi }
        .flatMapLatest { wifi ->
            if (!wifi) flowOf<ConnectionIssue?>(ConnectionIssue.NoWifi)
            else flow<ConnectionIssue?> {
                emit(null)
                delay(400) // brief gap so a retry visibly closes and re-opens the dialog
                emit(checkServer())
            }
        }

    fun retry() = retryTick.update { it + 1 }

    suspend fun checkServer(): ConnectionIssue? {
        val url = serverUrl ?: return null
        return withContext(Dispatchers.IO) {
            try {
                healthClient.newCall(Request.Builder().url(url).head().build()).execute().close()
                null
            } catch (e: Exception) {
                ConnectionIssue.ServerUnreachable(e.toUserMessage())
            }
        }
    }

    private fun NetworkCapabilities?.isWifi() = this != null &&
        (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
}
