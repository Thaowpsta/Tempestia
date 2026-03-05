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