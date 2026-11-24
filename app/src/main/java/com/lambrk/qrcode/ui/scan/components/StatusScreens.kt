package com.lambrk.qrcode.ui.scan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lambrk.qrcode.ui.scan.ScanUiState

private val PassGreen = Color(0xFF2E7D32)
private val FailRed = Color(0xFFC62828)

@Composable
private fun StatusScaffold(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    actions: @Composable () -> Unit
) {
    // Surface supplies the matching "on surface" text color; without it text defaults to black.
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.statusBarsPadding().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { content() }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { actions() }
        }
    }
}

@Composable
fun PermissionScreen(onGrant: () -> Unit) {
    StatusScaffold(
        content = {
            Text(
                "Camera access needed",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Allow camera access so the app can scan equipment and material QR codes.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        },
        actions = {
            Button(onClick = onGrant, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("Allow camera")
            }
        }
    )
}

@Composable
fun EquipmentDoneScreen(state: ScanUiState, onStartMaterial: () -> Unit, onRescan: () -> Unit) {
    StatusScaffold(
        content = {
            AnimatedCheck(PassGreen)
            Text(
                "Equipment scanned",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                "Move the phone away, then scan the material QR code.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            Column(Modifier.fillMaxWidth().padding(top = 32.dp)) {
                ValueRow("Equipment", state.equipmentCode)
            }
        },
        actions = {
            Button(onClick = onStartMaterial, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("Start material scan")
            }
            OutlinedButton(onClick = onRescan, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Text("Rescan equipment", modifier = Modifier.padding(start = 8.dp))
            }
        }
    )
}

@Composable
fun ResultScreen(state: ScanUiState, onScanAnother: () -> Unit) {
    val ok = state.passed
    val circle = if (ok) PassGreen else FailRed
    // Lighter tint for text on dark surfaces so it stays readable.
    val textColor = if (isSystemInDarkTheme()) {
        if (ok) Color(0xFF81C784) else Color(0xFFEF9A9A)
    } else circle
    StatusScaffold(
        content = {
            if (ok) AnimatedCheck(circle) else AnimatedCross(circle)
            Text(
                if (ok) "PASS" else "FAIL",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold, color = textColor,
                modifier = Modifier.padding(top = 20.dp)
            )
            state.failureMessage?.let {
                Text(
                    it, textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Text(
                if (state.checkedOnline) "Verified with server" else "Checked offline",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            Column(Modifier.fillMaxWidth().padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ValueRow("Equipment", state.equipmentCode)
                ValueRow("Material", state.materialCode)
            }
        },
        actions = {
            Button(onClick = onScanAnother, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Text("Scan another", modifier = Modifier.padding(start = 8.dp))
            }
        }
    )
}
