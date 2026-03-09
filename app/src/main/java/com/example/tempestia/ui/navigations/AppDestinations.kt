package com.example.tempestia.ui.navigations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.tempestia.R

enum class AppDestinations(
    val label: Int,
    val icon: ImageVector,
) {
    HOME(R.string.nav_home, Icons.Default.Home),
    FAVORITES(R.string.nav_favorites, Icons.Default.LocationOn),
    PROFILE(R.string.nav_alerts, Icons.Default.NotificationsNone),
    SETTINGS(R.string.nav_settings, Icons.Default.Settings),
}