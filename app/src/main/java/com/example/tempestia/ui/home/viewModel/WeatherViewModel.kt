package com.example.tempestia.ui.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tempestia.data.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _weatherState: MutableStateFlow<WeatherState> = MutableStateFlow(WeatherState.Idle)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private val apiKey = "550c8ee3f0df2a116a6eeea38a140149"

    fun getWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading

            try {
                val weatherResponse = repository.getWeather(lat, lon, apiKey)
                val geoResponse = repository.getCityName(lat, lon, apiKey)

                val weatherBody = weatherResponse.body()
                val geoBody = geoResponse.body()

                if (weatherResponse.isSuccessful && weatherBody != null) {

                    val finalCityName = if (geoResponse.isSuccessful && !geoBody.isNullOrEmpty()) {
                        geoBody.first().name // This will be "Alexandria"
                    } else {
                        weatherBody.cityName
                    }

                    val updatedWeatherData = weatherBody.copy(cityName = finalCityName)

                    _weatherState.value = WeatherState.Success(updatedWeatherData)
                } else {
                    _weatherState.value = WeatherState.Error("Network Error: ${weatherResponse.code()}")
                }
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error(e.localizedMessage ?: "Unknown Error occurred")
            }
        }
    }
}