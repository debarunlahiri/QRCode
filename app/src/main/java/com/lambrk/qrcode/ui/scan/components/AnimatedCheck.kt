package com.lambrk.qrcode.ui.scan.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Success icon: the circle pops in with a bounce, then the check mark draws itself. */
@Composable
fun AnimatedCheck(color: Color, modifier: Modifier = Modifier.size(104.dp)) {
    val scale = remember { Animatable(0f) }
    val draw = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
        draw.animateTo(1f, tween(350, easing = FastOutSlowInEasing))
    }
    Canvas(modifier) {
        drawCircle(color, radius = size.minDimension / 2f * scale.value)
        val check = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.52f)
            lineTo(size.width * 0.44f, size.height * 0.67f)
            lineTo(size.width * 0.74f, size.height * 0.36f)
        }
        val measure = PathMeasure().apply { setPath(check, false) }
        val visible = Path()
        measure.getSegment(0f, measure.length * draw.value, visible, true)
        drawPath(
            visible, Color.White,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/** Failure icon: the circle pops in with a bounce, then the two strokes of the cross draw one after the other. */
@Composable
fun AnimatedCross(color: Color, modifier: Modifier = Modifier.size(104.dp)) {
    val scale = remember { Animatable(0f) }
    val draw = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
        draw.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }
    Canvas(modifier) {
        drawCircle(color, radius = size.minDimension / 2f * scale.value)
        val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = size.width
        val h = size.height
        val first = (draw.value * 2f).coerceIn(0f, 1f)
        val second = (draw.value * 2f - 1f).coerceIn(0f, 1f)
        if (first > 0f) drawLine(
            Color.White, Offset(w * 0.34f, h * 0.34f),
            Offset(w * (0.34f + 0.32f * first), h * (0.34f + 0.32f * first)),
            strokeWidth = stroke.width, cap = StrokeCap.Round
        )
        if (second > 0f) drawLine(
            Color.White, Offset(w * 0.66f, h * 0.34f),
            Offset(w * (0.66f - 0.32f * second), h * (0.34f + 0.32f * second)),
            strokeWidth = stroke.width, cap = StrokeCap.Round
        )
    }
}
