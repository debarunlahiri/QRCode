package com.lambrk.qrcode.ui.scan.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private val FrameSize = 260.dp

/** Dimmed screen with a clear rounded scan window, corner brackets and a moving scan line. */
@Composable
fun ScanOverlay(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "scanline")
    val progress by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "progress"
    )
    Canvas(Modifier.fillMaxSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        val side = FrameSize.toPx()
        val tl = Offset((size.width - side) / 2f, (size.height - side) / 2f - 40.dp.toPx())
        val radius = 28.dp.toPx()
        drawRect(Color.Black.copy(alpha = 0.55f))
        drawRoundRect(Color.Black, tl, Size(side, side), CornerRadius(radius), blendMode = BlendMode.Clear)
        drawBrackets(tl, side, radius)
        if (active) {
            val y = tl.y + 16.dp.toPx() + (side - 32.dp.toPx()) * progress
            drawLine(
                Color.White.copy(alpha = 0.9f),
                Offset(tl.x + 20.dp.toPx(), y), Offset(tl.x + side - 20.dp.toPx(), y),
                strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawBrackets(tl: Offset, side: Float, r: Float) {
    val len = 40.dp.toPx()
    val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
    fun corner(x: Float, y: Float, sx: Float, sy: Float) {
        val p = Path().apply {
            moveTo(x, y + sy * len)
            lineTo(x, y + sy * r)
            quadraticTo(x, y, x + sx * r, y)
            lineTo(x + sx * len, y)
        }
        drawPath(p, Color.White, style = stroke)
    }
    corner(tl.x, tl.y, 1f, 1f)
    corner(tl.x + side, tl.y, -1f, 1f)
    corner(tl.x, tl.y + side, 1f, -1f)
    corner(tl.x + side, tl.y + side, -1f, -1f)
}
