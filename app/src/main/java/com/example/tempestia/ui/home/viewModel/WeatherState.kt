package com.example.tempestia.ui.home.viewModel

import com.example.tempestia.data.forecast.model.WeatherResponse

sealed class WeatherState {
    object Idle : WeatherState()
    object Loading : WeatherState()
    data class Success(val weatherData: WeatherResponse) : WeatherState()
    data class Error(val message: String) : WeatherState()
}