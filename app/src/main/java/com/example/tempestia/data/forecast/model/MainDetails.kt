package com.example.tempestia.data.forecast.model

import com.google.gson.annotations.SerializedName

data class MainDetails(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    val temp_min: Double,
    val temp_max: Double,
    val humidity: Int
)