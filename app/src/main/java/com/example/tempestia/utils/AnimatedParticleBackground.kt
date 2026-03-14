package com.example.tempestia.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import kotlin.random.Random

@Composable
fun AnimatedParticleBackground() {
    val colors = LocalTempestiaColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing)),
        label = "time"
    )

    val particles = remember {
        val random = Random(42)
        List(40) {
            floatArrayOf(
                random.nextFloat(),
                random.nextFloat() * 1.5f + 0.5f,
                random.nextFloat(),
                random.nextFloat() * 3f + 1f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerGlowColors = if (colors.isDark) {
            listOf(Color(0xFF1E1136), Color(0xFF130D26), colors.bgDeep)
        } else {
            listOf(Color(0xFFD8CCF5), Color(0xFFEDE8F8), colors.bgDeep)
        }

        drawRect(
            brush = Brush.radialGradient(
                colors = centerGlowColors,
                center = Offset(size.width / 2, 0f),
                radius = size.height * 0.7f
            )
        )

        particles.forEach { p ->
            val localTime = (time * p[1] + p[2]) % 1f
            val currentY = size.height * (1f - localTime)
            val currentX = (size.width * p[0]) + (localTime * size.width * 0.15f)

            val alpha = when {
                localTime < 0.2f -> localTime / 0.2f
                localTime > 0.8f -> (1f - localTime) / 0.2f
                else -> 1f
            }

            drawCircle(
                color = colors.purpleCore.copy(alpha = alpha * 0.3f),
                radius = p[3],
                center = Offset(currentX, currentY)
            )
        }
    }
}