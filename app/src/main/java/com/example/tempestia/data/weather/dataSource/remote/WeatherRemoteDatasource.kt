package com.example.tempestia.data.weather.dataSource.remote

import com.example.tempestia.data.network.RetrofitClient
import com.example.tempestia.data.network.WeatherService
import java.util.Locale

class WeatherRemoteDatasource {
    private val weatherService: WeatherService = RetrofitClient.apiService

    suspend fun getCurrentWeather(lat: Double, lon: Double, apiKey: String) =
        weatherService.getCurrentWeather(
            lat = lat,
            lon = lon,
            apiKey = apiKey,
            language = Locale.getDefault().language
        )

    suspend fun getCityName(lat: Double, lon: Double, apiKey: String) =
        weatherService.getCityName(lat = lat, lon = lon, apiKey = apiKey)

    suspend fun getCoordinatesByName(query: String, apiKey: String, limit: Int = 5) =
        weatherService.getCoordinatesByName(query = query, limit = limit, apiKey = apiKey)
}