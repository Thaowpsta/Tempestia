package com.example.tempestia.data.forecast.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val weather: List<Weather>,
    val main: MainDetails,
    @SerializedName("name")
    val cityName: String,
    val wind: Wind,
)