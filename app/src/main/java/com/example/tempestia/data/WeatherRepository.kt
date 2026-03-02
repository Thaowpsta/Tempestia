package com.example.tempestia.data

import com.example.tempestia.data.network.RetrofitClient


class WeatherRepository {
    private val api = RetrofitClient.apiService

    suspend fun getWeather(lat: Double, lon: Double, apiKey: String) = 
        api.getCurrentWeather(lat, lon, apiKey)

    suspend fun getCityName(lat: Double, lon: Double, apiKey: String) =
        api.getCityName(lat, lon, 1, apiKey)
}