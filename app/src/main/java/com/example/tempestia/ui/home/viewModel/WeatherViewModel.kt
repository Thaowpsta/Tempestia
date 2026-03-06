package com.example.tempestia.ui.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tempestia.BuildConfig
import com.example.tempestia.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _weatherState: MutableStateFlow<WeatherState> = MutableStateFlow(WeatherState.Idle)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private var lastRequestedLat: Double? = null
    private var lastRequestedLon: Double? = null

    private val apiKey = BuildConfig.WEATHER_API_KEY

    fun getWeather(lat: Double, lon: Double) {

        if (lat == lastRequestedLat && lon == lastRequestedLon && _weatherState.value !is WeatherState.Error) {
            return
        }

        lastRequestedLat = lat
        lastRequestedLon = lon

        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading

            try {
                val weatherResponse = repository.getWeather(lat, lon, apiKey)
                val geoResponse = repository.getCityName(lat, lon, apiKey)

                val weatherBody = weatherResponse.body()
                val geoBody = geoResponse.body()

                if (weatherResponse.isSuccessful && weatherBody != null) {
                    val finalCityName = geoBody?.firstOrNull()?.name
                        ?: weatherBody.timezone.substringAfterLast("/")

                    _weatherState.value = WeatherState.Success(weatherBody, finalCityName)
                } else {
                    _weatherState.value = WeatherState.Error("Network Error: ${weatherResponse.code()}")
                }
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error(e.localizedMessage ?: "Unknown Error occurred")
            }
        }
    }
}

class WeatherViewModelFactory(
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}