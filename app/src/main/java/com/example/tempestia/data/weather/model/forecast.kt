package com.example.tempestia.data.weather.model

import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    val pressure: Int,
    val humidity: Int,
    val visibility: Int,
    @SerializedName("wind_speed")
    val windSpeed: Double,
    val uvi: Double,
    val weather: List<WeatherIcon>
)

data class HourlyWeather(
    val dt: Long,
    val temp: Double,
    val weather: List<WeatherIcon>
)

data class DailyWeather(
    val dt: Long,
    val temp: DailyTemp,
    val weather: List<WeatherIcon>
)

data class DailyTemp(
    val min: Double,
    val max: Double
)

data class WeatherIcon(
    val id: Int,
    val description: String,
    val icon: String
)

data class WeatherAlert(
    @SerializedName("sender_name")
    val senderName: String,
    val event: String,
    val start: Long,
    val end: Long,
    val description: String,
    val tags: List<String>? = null
)