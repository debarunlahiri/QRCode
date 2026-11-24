package com.lambrk.qrcode.ui.scan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lambrk.qrcode.domain.model.ScanStep

@Composable
fun StepIndicator(step: ScanStep) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepDot(1, "Equipment", done = step != ScanStep.EQUIPMENT, current = step == ScanStep.EQUIPMENT)
        Box(
            Modifier.padding(horizontal = 8.dp).size(width = 48.dp, height = 2.dp)
                .background(if (step == ScanStep.EQUIPMENT) Color.White.copy(0.4f) else Color.White)
        )
        StepDot(
            2, "Material", done = step == ScanStep.DONE,
            current = step == ScanStep.MATERIAL || step == ScanStep.EQUIPMENT_DONE
        )
    }
}

@Composable
private fun StepDot(number: Int, label: String, done: Boolean, current: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(32.dp).background(
                if (done || current) Color.White else Color.White.copy(0.25f), CircleShape
            ),
            Alignment.Center
        ) {
            if (done) Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(18.dp))
            else Text(
                "$number",
                color = if (current) Color.Black else Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
    }
}
