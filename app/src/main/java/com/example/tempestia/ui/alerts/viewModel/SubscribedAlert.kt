package com.example.tempestia.ui.alerts.viewModel

import com.example.tempestia.worker.NotificationType

data class SubscribedAlert(
    val id: String,
    val title: String,
    val subtitle: String,
    val level: AlertLevel,
    val notificationType: NotificationType,
    val isActive: Boolean
)