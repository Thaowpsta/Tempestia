package com.example.tempestia.ui.settings.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tempestia.data.favorites.model.FavoriteCity
import com.example.tempestia.repository.WeatherRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: WeatherRepository) : ViewModel() {

    val isCelsiusFlow = repository.isCelsiusFlow
    val is24HourFlow = repository.is24HourFlow
    val themeModeFlow = repository.themeModeFlow
    val languageFlow = repository.languageFlow
    val locationNameFlow = repository.locationFlow.map { loc ->
        if (loc != null) {
            repository.getPreciseLocationName(loc.first, loc.second) ?: "Unknown Location"
        } else {
            "Location not set"
        }
    }

    fun setCelsius(isCelsius: Boolean) {
        viewModelScope.launch { repository.saveIsCelsius(isCelsius) }
    }

    fun set24Hour(is24Hour: Boolean) {
        viewModelScope.launch { repository.saveIs24Hour(is24Hour) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.saveThemeMode(mode) }
    }

    fun saveLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.saveLocation(lat, lng)

            val cityName = repository.getPreciseLocationName(lat, lng) ?: "Unknown Location"
            val newFavorite = FavoriteCity(
                cityName = cityName,
                lat = lat,
                lon = lng,
                isCurrentLocation = true
            )
            repository.setCityAsCurrent(newFavorite)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            repository.saveLanguage(language)

            val savedCities = repository.getAllFavoritesSync()

            for (city in savedCities) {
                val translatedName = repository.getPreciseLocationName(city.lat, city.lon, languageTag = language)

                if (translatedName != null && translatedName != city.cityName) {
                    repository.updateFavorite(city.copy(cityName = translatedName))
                }
            }
        }
    }
}

class SettingsViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}