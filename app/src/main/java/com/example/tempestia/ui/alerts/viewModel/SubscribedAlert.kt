package com.example.tempestia.ui.alerts.viewModel

import com.example.tempestia.data.weather.model.AlertLevel
import com.example.tempestia.ui.alerts.worker.NotificationType

data class SubscribedAlert(
    val id: String,
    val title: String,
    val subtitle: String,
    val level: AlertLevel,
    val notificationType: NotificationType,
    val isActive: Boolean,
    val timeHour: Int? = null,
    val timeMinute: Int? = null
)