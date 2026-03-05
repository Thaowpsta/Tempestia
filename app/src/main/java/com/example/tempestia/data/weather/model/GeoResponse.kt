package com.example.tempestia.data.weather.model

data class GeoResponse(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String? = null,
    val state: String? = null
)