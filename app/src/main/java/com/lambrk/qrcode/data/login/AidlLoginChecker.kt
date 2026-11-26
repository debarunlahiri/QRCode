package com.lambrk.qrcode.data.login

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.loginapp.ILoginService
import com.lambrk.qrcode.domain.model.LoginState
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Asks the login app, over AIDL, whether a user is signed in.
 * Binds to its service, calls [ILoginService.isLoggedIn], then unbinds.
 * Any failure (app missing, service refused, timeout, remote crash) is reported as NotLoggedIn.
 */
class AidlLoginChecker(
    context: Context,
    private val timeoutMs: Long = 4_000
) : ExternalLoginChecker {

    private val appContext = context.applicationContext

    override suspend fun check(): LoginState =
        withTimeoutOrNull(timeoutMs) { bindAndQuery() } ?: LoginState.NotLoggedIn

    private suspend fun bindAndQuery(): LoginState {
        var connection: ServiceConnection? = null
        try {
            return suspendCancellableCoroutine { cont ->
                val conn = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                        // Binder calls can block; keep them off the main thread that delivers this callback.
                        Thread { cont.resumeOnce(query(binder)) }.start()
                    }

                    override fun onServiceDisconnected(name: ComponentName) = Unit
                    override fun onNullBinding(name: ComponentName) = cont.resumeOnce(LoginState.NotLoggedIn)
                    override fun onBindingDied(name: ComponentName) = cont.resumeOnce(LoginState.NotLoggedIn)
                }
                connection = conn

                val intent = Intent(ExternalAppConfig.SERVICE_ACTION)
                    .setPackage(ExternalAppConfig.PACKAGE_NAME)
                val bound = try {
                    appContext.bindService(intent, conn, Context.BIND_AUTO_CREATE)
                } catch (e: SecurityException) {
                    false
                }
                if (!bound) cont.resumeOnce(LoginState.NotLoggedIn)
            }
        } finally {
            // Runs on result, timeout and cancellation alike. Also required after a failed bind.
            connection?.let { runCatching { appContext.unbindService(it) } }
        }
    }

    private fun query(binder: IBinder): LoginState = try {
        val service = ILoginService.Stub.asInterface(binder)
        if (service.isLoggedIn) LoginState.LoggedIn(service.userName?.takeIf { it.isNotBlank() })
        else LoginState.NotLoggedIn
    } catch (e: Exception) {
        LoginState.NotLoggedIn
    }

    private fun CancellableContinuation<LoginState>.resumeOnce(state: LoginState) {
        if (isActive) resume(state)
    }
}
