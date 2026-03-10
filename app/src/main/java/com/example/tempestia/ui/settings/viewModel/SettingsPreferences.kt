package com.example.tempestia.ui.settings.viewModel

data class SettingsPreferences(
    val isCelsius: Boolean = true,
    val is24Hour: Boolean = false,
    val themeMode: String = "System",
    val language: String = "en",
    val locationName: String = "Location not set",
    val locationMethod: String = "GPS",
    val showMapDialog: Boolean = false
)
