package com.example.tempestia.data.weather.model

data class DailyWeather(
    val dt: Long,
    val temp: DailyTemp,
    val weather: List<WeatherIcon>
)