package com.chandra.practice.deviceinfo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

/** A minimal line chart for a rolling window of values — no axes/labels, just the trend. */
@Composable
fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    minValue: Float = 0f,
) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val maxValue = (values.maxOrNull() ?: 1f).coerceAtLeast(minValue + 1f)
        val range = (maxValue - minValue).coerceAtLeast(1f)

        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index / (values.size - 1).toFloat() * size.width
            val y = size.height - ((value - minValue) / range) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
