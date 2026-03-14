package com.example.tempestia.ui.onboarding.view.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors

@Composable
fun PermissionRow(icon: ImageVector, title: String, desc: String) {
    val colors = LocalTempestiaColors.current
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(colors.glass, RoundedCornerShape(16.dp))
            .border(1.dp, colors.glassBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp)
                .background(
                    Brush.linearGradient(listOf(colors.purpleCore.copy(alpha = 0.2f), colors.purpleBright.copy(alpha = 0.1f))),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.purpleCore, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, color = colors.text1, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = desc, color = colors.text3, fontSize = 13.sp)
        }
    }
}

fun Modifier.centerGlow(
    color: Color,
    radiusDp: Dp,
    cornerRadiusDp: Dp
) = this.drawBehind {
    this.drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()

        frameworkPaint.color = android.graphics.Color.WHITE
        frameworkPaint.setShadowLayer(
            radiusDp.toPx(),
            0f,
            0f,
            color.copy(alpha = 0.8f).toArgb()
        )

        canvas.drawRoundRect(
            0f, 0f, size.width, size.height,
            cornerRadiusDp.toPx(), cornerRadiusDp.toPx(),
            paint
        )
    }
}

@Composable
fun PulsingButton(text: String, onClick: () -> Unit) {
    val colors = LocalTempestiaColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "btn")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOut), RepeatMode.Reverse),
        label = "btn_pulse_glow"
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.centerGlow(
            color = colors.purpleSoft,
            radiusDp = pulseGlow.dp,
            cornerRadiusDp = 50.dp
        )
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(colors.purpleCore, colors.purpleBright)), RoundedCornerShape(50.dp))
                .padding(horizontal = 40.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}