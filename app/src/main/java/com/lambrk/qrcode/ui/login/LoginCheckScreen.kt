package com.lambrk.qrcode.ui.login

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lambrk.qrcode.data.login.ExternalAppConfig
import com.lambrk.qrcode.data.remote.ApiConfig
import com.lambrk.qrcode.domain.model.LoginState
import com.lambrk.qrcode.ui.components.AnimatedGradientBackground

/** Shown while we detect (or wait for) a login made in the other app. */
@Composable
fun LoginCheckScreen(
    state: LoginState,
    onCheckAgain: () -> Unit,
    onContinueWithoutLogin: () -> Unit
) {
    val context = LocalContext.current
    fun openLoginApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(ExternalAppConfig.PACKAGE_NAME)
        if (intent != null) context.startActivity(intent)
        else Toast.makeText(context, "${ExternalAppConfig.DISPLAY_NAME} is not installed", Toast.LENGTH_LONG).show()
    }

    AnimatedGradientBackground {
        Box(Modifier.fillMaxSize().systemBarsPadding().padding(24.dp), Alignment.Center) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(state, label = "login-state", modifier = Modifier.padding(28.dp)) { s ->
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (s is LoginState.NotLoggedIn) {
                            Box(
                                Modifier.size(72.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Text(
                                "Login required",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Sign in with the ${ExternalAppConfig.DISPLAY_NAME}, then come back here. " +
                                    "We will detect it automatically.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = ::openLoginApp,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                            ) { Text("Open ${ExternalAppConfig.DISPLAY_NAME}") }
                            OutlinedButton(
                                onClick = onCheckAgain,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Text("Check again", modifier = Modifier.padding(start = 8.dp))
                            }
                            if (ApiConfig.isDevelopment) {
                                TextButton(onClick = onContinueWithoutLogin) {
                                    Text("Continue without login (development only)")
                                }
                            }
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(56.dp))
                            Text(
                                "Checking login",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Looking for a signed-in session in the ${ExternalAppConfig.DISPLAY_NAME}.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
