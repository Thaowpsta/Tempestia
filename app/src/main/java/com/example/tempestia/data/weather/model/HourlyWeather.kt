package com.example.tempestia.data.weather.model

data class HourlyWeather(
    val dt: Long,
    val temp: Double,
    val weather: List<WeatherIcon>
)