package com.lambrk.qrcode.ui.scan.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Label + scanned value. The value can be any length (API-driven), so it is height-capped and scrolls. */
@Composable
fun ValueRow(label: String, value: String?) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.heightIn(max = 72.dp).verticalScroll(rememberScrollState())) {
            Text(
                value ?: "Not scanned yet",
                style = MaterialTheme.typography.bodyMedium,
                color = if (value == null) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
