package com.example.tempestia.ui.alerts.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tempestia.ui.alerts.viewModel.AlertItem
import com.example.tempestia.ui.alerts.viewModel.AlertLevel
import com.example.tempestia.ui.alerts.viewModel.AlertsViewModel
import com.example.tempestia.ui.onboarding.view.AnimatedParticleBackground
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors

@Composable
fun AlertsScreen(viewModel: AlertsViewModel = viewModel()) {
    val colors = LocalTempestiaColors.current
    val alerts by viewModel.alerts.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(colors.bgDeep)) {
        AnimatedParticleBackground()

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            Text(
                text = "Weather Alerts",
                color = colors.text1,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Choose which alerts you want to subscribe to.",
                color = colors.text3,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            ActiveStormBanner()

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    AlertCard(
                        alert = alert,
                        onToggle = { viewModel.toggleAlert(alert.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    AddAlertCard()
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: AlertItem, onToggle: () -> Unit) {
    val colors = LocalTempestiaColors.current

    val activeBorderAlpha = if (alert.isSubscribed) 0.4f else 0.0f
    val activeBgAlpha = if (alert.isSubscribed) 0.12f else 0.0f

    val baseColor = when (alert.level) {
        AlertLevel.DANGER -> Color(0xFFEF4444)
        AlertLevel.WARNING -> Color(0xFFF59E0B)
        AlertLevel.INFO -> colors.purpleCore
    }

    val borderColor by animateColorAsState(
        targetValue = if (alert.isSubscribed) baseColor.copy(alpha = activeBorderAlpha) else colors.glassBorder,
        label = "border"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (alert.isSubscribed) baseColor.copy(alpha = activeBgAlpha) else colors.glass,
        label = "bg"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(baseColor.copy(alpha = 0.2f))
                    .border(1.dp, baseColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (alert.level == AlertLevel.INFO) Icons.Filled.Info else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = baseColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = alert.title, color = colors.text1, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = alert.subtitle, color = colors.text3, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = alert.meta, color = colors.text3, fontSize = 13.sp)

            CustomToggle(checked = alert.isSubscribed, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
fun CustomToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = LocalTempestiaColors.current
    val thumbOffset by animateDpAsState(targetValue = if (checked) 20.dp else 0.dp, label = "thumb", animationSpec = tween(300))
    val bgColor by animateColorAsState(targetValue = if (checked) colors.purpleBright else colors.bgSurface, label = "bg")

    Box(
        modifier = Modifier
            .width(46.dp)
            .height(26.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, if (checked) Color.Transparent else colors.glassBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun ActiveStormBanner() {
    val colors = LocalTempestiaColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(1500), repeatMode = RepeatMode.Restart), label = "alpha"
    )
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 12f,
        animationSpec = infiniteRepeatable(animation = tween(1500), repeatMode = RepeatMode.Restart), label = "size"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size((12 + pulseSize).dp).clip(CircleShape).background(Color(0xFFEF4444).copy(alpha = pulseAlpha)))
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFEF4444)))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ACTIVE: SEVERE STORM WARNING",
                color = Color(0xFFEF4444),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Take shelter immediately. Expected winds up to 60km/h.",
                color = colors.text3,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun AddAlertCard() {
    val colors = LocalTempestiaColors.current
    val borderColor = colors.glassBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx())
                )
            }
            .background(colors.glass, RoundedCornerShape(22.dp))
            .clickable { /*TODO Add new alert logic */ }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.purpleBright),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Create Custom Alert", color = colors.text2, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Get notified for specific conditions", color = colors.text3, fontSize = 13.sp)
        }
    }
}