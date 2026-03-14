package com.example.tempestia.ui.onboarding.view.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempestia.R
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors

@Composable
fun SplashScreen(onEnter: () -> Unit) {
    val colors = LocalTempestiaColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -24f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    val spinSlow by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing)),
        label = "spin_slow"
    )
    val spinReverse by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing)),
        label = "spin_reverse"
    )

    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -1000f, targetValue = 2000f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "shimmer"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(240.dp).offset(y = floatY.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = spinReverse }) {
                drawCircle(
                    color = colors.purpleCore.copy(alpha = 0.2f),
                    style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), 0f))
                )
            }
            Box(modifier = Modifier.size(220.dp).graphicsLayer { rotationZ = spinSlow }) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = colors.purpleCore.copy(alpha = 0.3f), style = Stroke(width = 3f))
                    drawCircle(color = colors.purpleBright, radius = 8f, center = Offset(size.width / 2, 0f))
                }
            }

            Image(
                painter = painterResource(if (colors.isDark) R.drawable.tempestia_dark else R.drawable.tempestia_light),
                contentDescription = "Tempestia Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(200.dp).centerGlow(
                        color = colors.purpleBright,
                        radiusDp = 24.dp,
                        cornerRadiusDp = 100.dp).clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.app_name).uppercase(),
            style = TextStyle(
                brush = Brush.linearGradient(
                    colors = listOf(colors.goldLight, colors.purpleCore, colors.text1),
                    start = Offset(shimmerX, 0f),
                    end = Offset(shimmerX + 800f, 0f)
                ),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 2.sp
            )
        )
        Text(
            text = stringResource(R.string.app_tagline),
            color = colors.text3,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 4.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 60.dp)
        )

        PulsingButton(text = stringResource(R.string.begin_journey), onClick = onEnter)
    }
}
