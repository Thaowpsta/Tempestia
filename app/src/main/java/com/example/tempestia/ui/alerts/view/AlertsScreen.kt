package com.example.tempestia.ui.alerts.view

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tempestia.R
import com.example.tempestia.data.weather.model.AlertLevel
import com.example.tempestia.data.weather.model.AlertItem
import com.example.tempestia.ui.alerts.viewModel.AlertsViewModel
import com.example.tempestia.ui.alerts.viewModel.SubscribedAlert
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import com.example.tempestia.ui.alerts.worker.NotificationType
import java.util.Calendar
import kotlin.math.roundToInt
import androidx.core.net.toUri
import com.example.tempestia.utils.AnimatedParticleBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(viewModel: AlertsViewModel = viewModel()) {
    val colors = LocalTempestiaColors.current
    val context = LocalContext.current
    val currentConfig = LocalConfiguration.current
    val currentLayoutDir = LocalLayoutDirection.current
    val activityContext = LocalView.current.context

    val subscribedAlerts by viewModel.subscribedAlerts.collectAsState()
    val availableTemplates by viewModel.availableTemplatesFlow.collectAsState()

    val showAddSheet by viewModel.showAddSheet.collectAsState()
    val alertToEdit by viewModel.alertToEdit.collectAsState()
    val templateToAdd by viewModel.templateToAdd.collectAsState()
    val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setHasNotificationPermission(isGranted)
    }

    if (alertToEdit != null) {
        AlertDialog(
            onDismissRequest = { viewModel.setAlertToEdit(null) },
            containerColor = colors.bgCard.copy(alpha = 1f),
            titleContentColor = colors.text1,
            title = {
                Text(
                    stringResource(R.string.notification_settings),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(
                            R.string.notify_prompt,
                            getLocalizedAlertText(alertToEdit!!.title)
                        ),
                        color = colors.text3
                    )

                    NotificationChoiceRow(
                        stringResource(R.string.silent_notif),
                        Icons.Filled.NotificationsOff,
                        isSelected = alertToEdit!!.notificationType == NotificationType.SILENT
                    ) {
                        viewModel.updateAlert(
                            alertToEdit!!,
                            newType = NotificationType.SILENT,
                            context = context
                        )
                        viewModel.setAlertToEdit(null)
                    }
                    NotificationChoiceRow(
                        stringResource(R.string.push_notif),
                        Icons.Filled.Notifications,
                        isSelected = alertToEdit!!.notificationType == NotificationType.PUSH
                    ) {
                        viewModel.updateAlert(
                            alertToEdit!!,
                            newType = NotificationType.PUSH,
                            context = context
                        )
                        viewModel.setAlertToEdit(null)
                    }
                    NotificationChoiceRow(
                        stringResource(R.string.sound_notif),
                        Icons.Filled.NotificationsActive,
                        isSelected = alertToEdit!!.notificationType == NotificationType.SOUND
                    ) {
                        viewModel.updateAlert(
                            alertToEdit!!,
                            newType = NotificationType.SOUND,
                            context = context
                        )
                        viewModel.setAlertToEdit(null)
                    }

                    NotificationChoiceRow(
                        stringResource(R.string.alarm_notif),
                        Icons.Filled.Alarm,
                        isUrgent = true,
                        isSelected = alertToEdit!!.notificationType == NotificationType.ALARM
                    ) {
                        // Alarm Permission (Android 12+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val alarmManager =
                                context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

                            if (!alarmManager.canScheduleExactAlarms()) {
                                val intent =
                                    android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        .apply {
                                            data =
                                                "package:${context.packageName}".toUri()

                                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                context.startActivity(intent)
                                return@NotificationChoiceRow
                            }
                        }

                        // "Display Over Other Apps" Permission
                        if (!android.provider.Settings.canDrawOverlays(context)) {
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.display_over_apps_prompt),
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${context.packageName}".toUri()
                            ).apply {
                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            }

                            context.startActivity(intent)
                            return@NotificationChoiceRow
                        }

                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            activityContext,
                            { _, selectedHour, selectedMinute ->
                                viewModel.updateAlert(
                                    alertToEdit!!,
                                    newType = NotificationType.ALARM,
                                    hour = selectedHour,
                                    minute = selectedMinute,
                                    context = context
                                )
                                viewModel.setAlertToEdit(null)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        ).show()
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.setAlertToEdit(null) }) {
                    Text(stringResource(R.string.cancel_btn), color = colors.text3)
                }
            }
        )
    }

    if (templateToAdd != null) {
        val handleStandardAdd = { notifType: NotificationType ->
            viewModel.addAlert(templateToAdd!!, notifType)
            viewModel.setTemplateToAdd(null)
            viewModel.setShowAddSheet(false)
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        AlertDialog(
            onDismissRequest = { viewModel.setTemplateToAdd(null) },
            containerColor = colors.bgCard.copy(alpha = 1f),
            titleContentColor = colors.text1,
            title = {
                Text(
                    stringResource(R.string.add_alert_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(
                            R.string.notify_prompt,
                            getLocalizedAlertText(templateToAdd!!.title)
                        ),
                        color = colors.text3
                    )

                    NotificationChoiceRow(
                        stringResource(R.string.silent_notif),
                        Icons.Filled.NotificationsOff
                    ) {
                        handleStandardAdd(NotificationType.SILENT)
                    }
                    NotificationChoiceRow(
                        stringResource(R.string.push_notif),
                        Icons.Filled.Notifications
                    ) {
                        handleStandardAdd(NotificationType.PUSH)
                    }
                    NotificationChoiceRow(
                        stringResource(R.string.sound_notif),
                        Icons.Filled.NotificationsActive
                    ) {
                        handleStandardAdd(NotificationType.SOUND)
                    }

                    NotificationChoiceRow(
                        stringResource(R.string.alarm_notif),
                        Icons.Filled.Alarm,
                        isUrgent = true
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val alarmManager =
                                context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                            if (!alarmManager.canScheduleExactAlarms()) {
                                val intent =
                                    android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        .apply {
                                            data =
                                                "package:${context.packageName}".toUri()

                                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                context.startActivity(intent)
                                return@NotificationChoiceRow
                            }
                        }
                        if (!android.provider.Settings.canDrawOverlays(context)) {
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.display_over_apps_prompt),
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            ).apply {
                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                            return@NotificationChoiceRow
                        }

                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            activityContext,
                            { _, selectedHour, selectedMinute ->
                                viewModel.addAlert(
                                    templateToAdd!!,
                                    NotificationType.ALARM,
                                    hour = selectedHour,
                                    minute = selectedMinute
                                )
                                viewModel.setTemplateToAdd(null)
                                viewModel.setShowAddSheet(false)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
                        ).show()
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.setTemplateToAdd(null) }) {
                    Text(stringResource(R.string.cancel_btn), color = colors.text3)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        AnimatedParticleBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            Text(
                text = stringResource(R.string.weather_alerts_title),
                color = colors.text1,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (!hasNotificationPermission) {
                PermissionWarningBanner(
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (subscribedAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.NotificationsOff,
                            contentDescription = null,
                            tint = colors.text3,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.no_active_alerts_title),
                            color = colors.text1,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.no_alerts_desc),
                            color = colors.text3,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(subscribedAlerts, key = { it.id }) { alert ->
                        StableSwipeToDismissAlertCard(
                            alert = alert,
                            onDeleteRequest = { viewModel.removeAlert(alert.id) },
                            onClick = { viewModel.setAlertToEdit(alert) },
                            onToggle = { isActive ->
                                viewModel.updateAlert(
                                    alert,
                                    isActive = isActive,
                                    context = context
                                )
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.setShowAddSheet(true) },
            containerColor = colors.purpleBright,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 24.dp)
                .size(64.dp)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_alert_title),
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        if (showAddSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowAddSheet(false) },
                sheetState = sheetState,
                containerColor = colors.bgDeep,
                dragHandle = { BottomSheetDefaults.DragHandle(color = colors.text3) }
            ) {
                CompositionLocalProvider(
                    LocalContext provides context,
                    LocalConfiguration provides currentConfig,
                    LocalLayoutDirection provides currentLayoutDir
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .padding(bottom = 48.dp)
                    ) {
                        Text(
                            stringResource(R.string.subscribe_alert),
                            color = colors.text1,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (availableTemplates.isEmpty()) {
                            Text(
                                stringResource(R.string.all_alerts_subscribed),
                                color = colors.text3,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                                items(availableTemplates) { template ->
                                    TemplateRow(template) {
                                        viewModel.setTemplateToAdd(template)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionWarningBanner(onRequestPermission: () -> Unit) {
    val colors = LocalTempestiaColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Warning,
            contentDescription = "Warning",
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.notifications_disabled),
                color = colors.text1,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                stringResource(R.string.enable_perms_alerts),
                color = colors.text2,
                fontSize = 13.sp
            )
        }
        TextButton(onClick = onRequestPermission) {
            Text(
                stringResource(R.string.enable_btn),
                color = Color(0xFFF59E0B),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CustomToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = LocalTempestiaColors.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        label = "thumb",
        animationSpec = tween(300)
    )
    val bgColor by animateColorAsState(
        targetValue = if (checked) colors.purpleBright else colors.bgSurface,
        label = "bg"
    )

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
fun NotificationChoiceRow(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isUrgent: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val colors = LocalTempestiaColors.current
    val bgColor = if (isSelected) colors.purpleBright.copy(alpha = 0.2f) else colors.glass
    val borderColor = if (isSelected) colors.purpleBright else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isUrgent) Color(0xFFFF4B4B) else colors.purpleBright
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = colors.text1, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TemplateRow(template: AlertItem, onClick: () -> Unit) {
    val colors = LocalTempestiaColors.current
    val iconColor = when (template.level) {
        AlertLevel.DANGER -> Color(0xFFEF4444)
        AlertLevel.WARNING -> Color(0xFFF59E0B)
        AlertLevel.INFO -> colors.purpleCore
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (template.level == AlertLevel.INFO) Icons.Filled.Info else Icons.Filled.Warning,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = getLocalizedAlertText(template.title),
                color = colors.text1,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = template.subtitle, color = colors.text3, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Filled.Add, contentDescription = "Add", tint = colors.purpleBright)
    }
}

@Composable
fun StableSwipeToDismissAlertCard(
    alert: SubscribedAlert,
    onDeleteRequest: () -> Unit,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {

    // This MUST remain mutableFloatStateOf. prevent UI jank.
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "swipe")
    val colors = LocalTempestiaColors.current
    val density = LocalDensity.current

    val iconAlpha = (-animatedOffsetX / 200f).coerceIn(0f, 1f)

    val bgAlpha = if (alert.isActive) 0.12f else 0.05f
    val baseColor = when (alert.level) {
        AlertLevel.DANGER -> Color(0xFFEF4444)
        AlertLevel.WARNING -> Color(0xFFF59E0B)
        AlertLevel.INFO -> colors.purpleCore
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(baseColor.copy(alpha = bgAlpha))
            .border(
                1.dp,
                baseColor.copy(alpha = if (alert.isActive) 0.4f else 0.1f),
                RoundedCornerShape(22.dp)
            )
    ) {
        if (animatedOffsetX < 0f) {
            val revealWidth = with(density) { (-animatedOffsetX).toDp() }
            Box(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(revealWidth)
                        .fillMaxHeight()
                        .background(Color(0xFFFF4B4B))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(end = 28.dp), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .alpha(iconAlpha)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -250f) onDeleteRequest()
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (dragAmount < 0 || offsetX < 0) {
                                offsetX = (offsetX + dragAmount).coerceAtMost(0f)
                            }
                        }
                    )
                }
                .clickable { onClick() }
                .padding(18.dp)
        ) {
            SubscribedAlertCardContent(alert, onToggle)
        }
    }
}

@Composable
fun SubscribedAlertCardContent(alert: SubscribedAlert, onToggle: (Boolean) -> Unit) {
    val colors = LocalTempestiaColors.current

    val textAlpha = if (alert.isActive) 1f else 0.5f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(textAlpha)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getLocalizedAlertText(alert.title),
                    color = colors.text1,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getLocalizedAlertText(alert.subtitle),
                    color = colors.text2,
                    fontSize = 14.sp
                )
            }

            CustomToggle(checked = alert.isActive, onCheckedChange = onToggle)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.glassBorder.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val (icon, typeText) = when (alert.notificationType) {
                NotificationType.SILENT -> Icons.Filled.NotificationsOff to stringResource(R.string.silent_notif_type)
                NotificationType.PUSH -> Icons.Filled.Notifications to stringResource(R.string.push_notif)
                NotificationType.SOUND -> Icons.Filled.NotificationsActive to stringResource(R.string.sound_notif_type)
                NotificationType.ALARM -> {
                    val timeStr = if (alert.timeHour != null && alert.timeMinute != null) {
                        val amPm =
                            if (alert.timeHour >= 12) stringResource(R.string.pm) else stringResource(
                                R.string.am
                            )
                        val hour12 = if (alert.timeHour % 12 == 0) 12 else alert.timeHour % 12
                        val minuteStr = alert.timeMinute.toString().padStart(2, '0')
                        stringResource(R.string.alarm_at_time, hour12, minuteStr, amPm)
                    } else stringResource(R.string.alarm_default)

                    Icons.Filled.Alarm to timeStr
                }
            }
            Icon(
                icon,
                contentDescription = null,
                tint = colors.text3,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = typeText,
                color = colors.text3,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun getLocalizedAlertText(englishText: String): String {
    return when (englishText) {
        "Morning Summary" -> stringResource(R.string.alert_title_morning)
        "Rain Reminder" -> stringResource(R.string.alert_title_rain)
        "Extreme Heat" -> stringResource(R.string.alert_title_heat)
        "Daily morning weather update" -> stringResource(R.string.alert_desc_morning)
        "Alerts you when rain is expected" -> stringResource(R.string.alert_desc_rain)
        "Alerts you when temps exceed 40°C" -> stringResource(R.string.alert_desc_heat)
        else -> englishText
    }
}