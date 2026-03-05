package com.example.tempestia.repository

import com.example.tempestia.data.network.RetrofitClient

class WeatherRepository {
    private val api = RetrofitClient.apiService

    suspend fun getWeather(lat: Double, lon: Double, apiKey: String) =
        api.getCurrentWeather(lat, lon, exclude = "minutely,alerts", apiKey, units = "metric")

    suspend fun getCityName(lat: Double, lon: Double, apiKey: String) =
        api.getCityName(lat, lon, limit = 1, apiKey)
}