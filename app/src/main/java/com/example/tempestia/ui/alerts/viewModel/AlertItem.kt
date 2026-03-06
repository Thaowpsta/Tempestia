package com.example.tempestia.ui.alerts.viewModel

data class AlertItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val level: AlertLevel,
    val meta: String,
    val isSubscribed: Boolean
)