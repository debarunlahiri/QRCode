package com.lambrk.qrcode.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lambrk.qrcode.QRCodeApp
import com.lambrk.qrcode.domain.model.LoginState
import com.lambrk.qrcode.domain.model.ConnectionIssue
import com.lambrk.qrcode.ui.components.ConnectionDialog
import com.lambrk.qrcode.ui.login.LoginCheckScreen
import com.lambrk.qrcode.ui.login.LoginViewModel
import com.lambrk.qrcode.ui.scan.ScanScreen

/** Login gate first, then the scan flow. The Wi-Fi dialog can appear on either. */
@Composable
fun AppRoot() {
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
    val login by loginViewModel.state.collectAsStateWithLifecycle()

    // Back from the other app: look again.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { loginViewModel.recheckIfNotLoggedIn() }

    when (login) {
        is LoginState.LoggedIn -> ScanScreen()
        else -> LoginCheckScreen(
            state = login,
            onCheckAgain = loginViewModel::check,
            onContinueWithoutLogin = loginViewModel::continueWithoutLogin
        )
    }

    val monitor = (LocalContext.current.applicationContext as QRCodeApp).container.networkMonitor
    val issue by monitor.issue.collectAsStateWithLifecycle(
        initialValue = if (monitor.isWifiConnected()) null else ConnectionIssue.NoWifi
    )
    // Keyed on the issue: dismissing lasts until the problem changes or comes back.
    var dismissed by remember(issue) { mutableStateOf(false) }
    issue?.takeIf { !dismissed }?.let {
        ConnectionDialog(
            issue = it,
            onDismiss = { dismissed = true },
            onRetry = if (it is ConnectionIssue.ServerUnreachable) monitor::retry else null
        )
    }
}
