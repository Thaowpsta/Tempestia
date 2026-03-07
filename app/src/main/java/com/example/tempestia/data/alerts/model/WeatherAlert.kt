package com.example.tempestia.data.alerts.model

import com.google.gson.annotations.SerializedName

data class WeatherAlert(
    @SerializedName("sender_name")
    val senderName: String,
    val event: String,
    val start: Long,
    val end: Long,
    val description: String,
    val tags: List<String>? = null
)