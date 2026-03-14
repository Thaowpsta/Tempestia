package com.example.tempestia.data.weather.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_alerts")
data class Alert(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val level: String,
    val notificationType: String,
    val isActive: Boolean = true,
    val timeHour: Int? = null,
    val timeMinute: Int? = null
)

data class AlertItem(
    val title: String,
    val subtitle: String,
    val level: AlertLevel
)