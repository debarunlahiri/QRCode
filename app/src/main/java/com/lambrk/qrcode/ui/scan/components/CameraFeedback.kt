package com.lambrk.qrcode.ui.scan.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import java.util.Locale

/** Ring drawn where the user tapped to focus. */
@Composable
fun FocusRing(center: Offset) {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(Color.White, radius = 36.dp.toPx(), center = center, style = Stroke(width = 2.dp.toPx()))
    }
}

/** Current pinch zoom level, e.g. "2.4x". */
@Composable
fun ZoomChip(ratio: Float, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.extraLarge, color = Color.Black.copy(alpha = 0.6f)) {
        Text(
            String.format(Locale.US, "%.1fx", ratio),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
