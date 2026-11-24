package com.lambrk.qrcode.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import kotlin.math.cos
import kotlin.math.sin

/**
 * Slowly drifting gradient built from the theme colors, so it looks right in light and dark mode.
 * One full revolution takes [periodMillis] (default 30 s).
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    periodMillis: Int = 30_000,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val colors = listOf(
        scheme.primaryContainer,
        scheme.tertiaryContainer,
        scheme.secondaryContainer,
        scheme.surfaceVariant
    )
    val transition = rememberInfiniteTransition(label = "gradient")
    val angle by transition.animateFloat(
        0f, (2 * Math.PI).toFloat(),
        infiniteRepeatable(tween(periodMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "angle"
    )
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.maxDimension * 0.6f
            val center = Offset(size.width / 2f, size.height / 2f)
            val dx = cos(angle) * radius
            val dy = sin(angle) * radius
            drawRect(
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(center.x - dx, center.y - dy),
                    end = Offset(center.x + dx, center.y + dy)
                )
            )
        }
        content()
    }
}
