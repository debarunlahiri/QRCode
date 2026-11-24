package com.lambrk.qrcode.ui.scan.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledIconToggleButton
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
import com.lambrk.qrcode.domain.model.ScanStep
import com.lambrk.qrcode.ui.scan.ScanUiState

/** Top step indicator + instruction, bottom sheet with scanned values and actions. */
@Composable
fun CameraChrome(
    state: ScanUiState,
    torchOn: Boolean,
    hasTorch: Boolean,
    onTorch: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepIndicator(state.step)
            Text(
                if (state.step == ScanStep.EQUIPMENT) "Scan the EQUIPMENT QR code"
                else "Scan the MATERIAL QR code",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp)
            )
            state.error?.let {
                Text(
                    it,
                    color = Color(0xFFFF8A80),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Box(Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.navigationBarsPadding().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ValueRow("Equipment", state.equipmentCode)
                ValueRow("Material", state.materialCode)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasTorch) {
                        FilledIconToggleButton(
                            checked = torchOn,
                            onCheckedChange = onTorch,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(if (torchOn) FlashOnIcon else FlashOffIcon, contentDescription = "Flashlight")
                        }
                    }
                    if (state.step == ScanStep.MATERIAL) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Text("Rescan equipment", modifier = Modifier.padding(start = 8.dp))
                        }
                    } else {
                        Text(
                            "Point the camera at the QR code. It scans automatically.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
