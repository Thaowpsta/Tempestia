package com.example.tempestia.ui.alerts.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlertsViewModel : ViewModel() {
    private val _alerts = MutableStateFlow(
        listOf(
            AlertItem("1", "Severe Thunderstorm", "Heavy rain and strong winds", AlertLevel.DANGER, "Notifies immediately", true),
            AlertItem("2", "Flash Flood Watch", "Potential for rapid urban flooding", AlertLevel.WARNING, "Notifies 1hr before", false),
            AlertItem("3", "Extreme Heat Advisory", "Temperatures exceeding 40°C", AlertLevel.WARNING, "Daily at 8:00 AM", true),
            AlertItem("4", "Daily Weather Summary", "Morning forecast and UV index", AlertLevel.INFO, "Daily at 7:00 AM", false)
        )
    )
    val alerts: StateFlow<List<AlertItem>> = _alerts.asStateFlow()

    fun toggleAlert(id: String) {
        _alerts.value = _alerts.value.map {
            if (it.id == id) it.copy(isSubscribed = !it.isSubscribed) else it
        }
    }
}