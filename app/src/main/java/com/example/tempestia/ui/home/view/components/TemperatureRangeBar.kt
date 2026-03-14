package com.example.tempestia.ui.home.view.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors

@Composable
fun TemperatureRangeBar(
    minTemp: Float,
    maxTemp: Float,
    weeklyMin: Float,
    weeklyMax: Float,
    modifier: Modifier = Modifier
) {
    val colors = LocalTempestiaColors.current

    Canvas(modifier = modifier.height(4.dp).fillMaxWidth()) {
        val range = weeklyMax - weeklyMin
        val safeRange = if (range == 0f) 1f else range

        val startX = size.width * ((minTemp - weeklyMin) / safeRange)
        val endX = size.width * ((maxTemp - weeklyMin) / safeRange)

        drawLine(
            color = colors.glassBorder,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )

        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(colors.purpleCore, colors.goldLight),
                startX = 0f,
                endX = size.width
            ),
            start = Offset(startX, size.height / 2),
            end = Offset(endX, size.height / 2),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
    }
}
