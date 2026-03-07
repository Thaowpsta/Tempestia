package com.example.tempestia.data.weather.model

import com.example.tempestia.data.alerts.model.WeatherAlert

data class WeatherResponse(
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
    val timezone: String ,
    val alerts: List<WeatherAlert>? = null
)