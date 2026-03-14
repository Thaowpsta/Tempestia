package com.example.tempestia.ui.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tempestia.BuildConfig
import com.example.tempestia.data.weather.model.WeatherResponse
import com.example.tempestia.data.WeatherRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _weatherState: MutableStateFlow<WeatherState> = MutableStateFlow(WeatherState.Idle)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    val isCelsiusFlow = repository.isCelsiusFlow
    val is24HourFlow = repository.is24HourFlow
    val languageFlow = repository.languageFlow
    private val apiKey = BuildConfig.WEATHER_API_KEY

    fun getWeather(lat: Double, lon: Double, isSwipeRefresh: Boolean = false) {
        viewModelScope.launch {

            if (!isSwipeRefresh) {
                _weatherState.value = WeatherState.Loading
            }

            val isOnline = repository.isNetworkAvailable()
            val lang = languageFlow.firstOrNull()

            if (isOnline) {
                try {
                    val response = repository.getWeather(lat, lon, apiKey)
                    if (response.isSuccessful && response.body() != null) {
                        val weather = response.body()!!
                        val cityName = repository.getPreciseLocationName(lat, lon, lang) ?: "Unknown Location"

                        // 🚨 Save Offline Cache
                        val city = repository.getCityByLatLng(lat, lon)
                        if (city != null) {
                            val json = Gson().toJson(weather)
                            repository.updateFavorite(city.copy(cachedWeather = json))
                        }

                        _weatherState.value = WeatherState.Success(weather, cityName)
                    } else {
                        loadCachedWeather(lat, lon, lang)
                    }
                } catch (_: Exception) {
                    loadCachedWeather(lat, lon, lang)
                }
            } else {
                loadCachedWeather(lat, lon, lang)
            }
        }
    }

    private suspend fun loadCachedWeather(lat: Double, lon: Double, lang: String? = null) {
        val city = repository.getCityByLatLng(lat, lon)
        if (city?.cachedWeather != null) {
            try {
                val weather = Gson().fromJson(city.cachedWeather, WeatherResponse::class.java)
                val cityName = repository.getPreciseLocationName(lat, lon, lang) ?: city.cityName
                _weatherState.value = WeatherState.Success(weather, cityName)
            } catch(_: Exception) {
                _weatherState.value = WeatherState.Error("Offline data corrupted.")
            }
        } else {
            _weatherState.value = WeatherState.Error("No internet connection. Please connect to view weather.")
        }
    }

    fun refreshWeather(isSwipeRefresh: Boolean = false) {
        viewModelScope.launch {

            if (!isSwipeRefresh) {
                _weatherState.value = WeatherState.Loading
            }

            val location = repository.locationFlow.firstOrNull()

            if (location != null) {
                getWeather(location.first, location.second, isSwipeRefresh)
            } else if (!isSwipeRefresh) {
                _weatherState.value = WeatherState.Error("No location set")
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