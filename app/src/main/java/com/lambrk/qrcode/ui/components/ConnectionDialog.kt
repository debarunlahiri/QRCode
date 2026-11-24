package com.lambrk.qrcode.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.lambrk.qrcode.domain.model.ConnectionIssue
import com.lambrk.qrcode.ui.scan.components.CloudOffIcon
import com.lambrk.qrcode.ui.scan.components.WifiOffIcon

/**
 * Error dialog with an icon for each [ConnectionIssue]. The app keeps working offline,
 * so every dialog can be dismissed. [onRetry] adds a Retry button (used for the server check).
 */
@Composable
fun ConnectionDialog(
    issue: ConnectionIssue,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val icon = if (issue is ConnectionIssue.NoWifi) WifiOffIcon else CloudOffIcon
    val title = when (issue) {
        is ConnectionIssue.NoWifi -> "Wi-Fi not connected"
        is ConnectionIssue.ServerUnreachable -> "Server not reachable"
        is ConnectionIssue.VerifyFailed -> "Server check failed"
    }
    val message = when (issue) {
        is ConnectionIssue.NoWifi ->
            "You can keep scanning offline and results are checked on this phone. " +
                "Connect to Wi-Fi to verify with the server."
        is ConnectionIssue.ServerUnreachable ->
            "Could not connect to the server: ${issue.reason}. Scanning still works offline."
        is ConnectionIssue.VerifyFailed ->
            "Could not verify with the server: ${issue.reason}. The result was checked on this phone."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            when {
                issue is ConnectionIssue.NoWifi -> TextButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    onDismiss()
                }) { Text("Wi-Fi settings") }
                onRetry != null -> TextButton(onClick = onRetry) { Text("Retry") }
                else -> TextButton(onClick = onDismiss) { Text("OK") }
            }
        },
        dismissButton = if (issue is ConnectionIssue.VerifyFailed) null else {
            { TextButton(onClick = onDismiss) { Text("Continue offline") } }
        }
    )
}
