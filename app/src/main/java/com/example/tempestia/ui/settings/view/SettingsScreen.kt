package com.example.tempestia.ui.settings.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempestia.ui.onboarding.view.AnimatedParticleBackground
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors

@Composable
fun SettingsScreen() {
    val colors = LocalTempestiaColors.current

    var isCelsius by remember { mutableStateOf(true) }
    var is24Hour by remember { mutableStateOf(false) }
    var themeMode by remember { mutableStateOf("System") }

    Box(modifier = Modifier.fillMaxSize().background(colors.bgDeep)) {

        AnimatedParticleBackground()

        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "Settings",
                    color = colors.text1,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "PREFERENCES",
                    color = colors.text3,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                )

                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Thermostat,
                        title = "Temperature Unit",
                        subtitle = "Used for all forecasts",
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SettingsChip("°C", isCelsius) { isCelsius = true }
                                SettingsChip("°F", !isCelsius) { isCelsius = false }
                            }
                        }
                    )

                    HorizontalDivider(color = colors.glassBorder.copy(alpha = 0.5f))

                    SettingsRow(
                        icon = Icons.Filled.AccessTime,
                        title = "Time Format",
                        subtitle = "12-hour or 24-hour clock",
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SettingsChip("12h", !is24Hour) { is24Hour = false }
                                SettingsChip("24h", is24Hour) { is24Hour = true }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "APPEARANCE",
                    color = colors.text3,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                )

                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Palette,
                        title = "App Theme",
                        subtitle = "Choose your aesthetic",
                        trailingContent = {
                            Column(
                                modifier = Modifier.width(160.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SettingsChip("Light", themeMode == "Light", Modifier.weight(1f)) { themeMode = "Light" }
                                    SettingsChip("Dark", themeMode == "Dark", Modifier.weight(1f)) { themeMode = "Dark" }
                                }
                                SettingsChip("System", themeMode == "System", Modifier.fillMaxWidth()) { themeMode = "System" }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Text(
                text = "Tempestia v1.0.0\nDesigned by Thaowpsta Saiid",
                color = colors.text3,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 120.dp, top = 16.dp)
            )
        }
    }
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalTempestiaColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.glass)
            .border(1.dp, colors.glassBorder, RoundedCornerShape(22.dp))
    ) {
        content()
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingContent: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalTempestiaColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.purpleBright.copy(alpha = 0.15f))
                .border(1.dp, colors.purpleBright.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.purpleBright, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            // maxLines = 1 ensures it stays on a single line!
            Text(
                text = title,
                color = colors.text1,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = colors.text3,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        trailingContent()
    }
}

@Composable
fun SettingsChip(
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalTempestiaColors.current

    val bgColor by animateColorAsState(if (isActive) colors.purpleCore else colors.glass, label = "bg")
    val textColor by animateColorAsState(if (isActive) Color.White else colors.text2, label = "text")
    val borderColor by animateColorAsState(if (isActive) Color.Transparent else colors.glassBorder, label = "border")

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}