package com.example.tempestia.ui.settings.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tempestia.R
import com.example.tempestia.data.favorites.model.FavoriteCity
import com.example.tempestia.data.WeatherRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _locationFetchState = MutableStateFlow<LocationFetchState>(LocationFetchState.Idle)
    val locationFetchState: StateFlow<LocationFetchState> = _locationFetchState.asStateFlow()

    private val _locationMethod = MutableStateFlow("GPS")
    private val _showMapDialog = MutableStateFlow(false)

    private val _toastMessage = MutableStateFlow<Int?>(null)
    val toastMessage: StateFlow<Int?> = _toastMessage.asStateFlow()

    val preferences: StateFlow<SettingsPreferences> = combine(
        repository.isCelsiusFlow,
        repository.is24HourFlow,
        repository.themeModeFlow,
        repository.languageFlow,
        repository.locationFlow
    ) { celsius, is24h, theme, lang, loc ->

        val locName = if (loc != null) {
            repository.getPreciseLocationName(loc.first, loc.second, lang) ?: "Unknown Location"
        } else "Location not set"

        SettingsPreferences(
            isCelsius = celsius,
            is24Hour = is24h,
            themeMode = theme,
            language = lang,
            locationName = locName
        )
    }.combine(_locationMethod) { partialPrefs, locMethod ->
        partialPrefs.copy(locationMethod = locMethod)
    }.combine(_showMapDialog) { partialPrefs, mapDialog ->
        partialPrefs.copy(showMapDialog = mapDialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsPreferences())

    fun setCelsius(isCelsius: Boolean) { viewModelScope.launch { repository.saveIsCelsius(isCelsius) } }
    fun set24Hour(is24Hour: Boolean) { viewModelScope.launch { repository.saveIs24Hour(is24Hour) } }
    fun setThemeMode(mode: String) { viewModelScope.launch { repository.saveThemeMode(mode) } }
    fun setLocationMethod(method: String) { _locationMethod.value = method }
    fun setShowMapDialog(show: Boolean) { _showMapDialog.value = show }

    fun showToast(resId: Int) { _toastMessage.value = resId }
    fun clearToast() { _toastMessage.value = null }

    fun fetchLocation() {
        viewModelScope.launch {
            _locationFetchState.value = LocationFetchState.Fetching

            val location = repository.fetchDeviceLocation()

            if (location != null) {
                saveLocation(location.latitude, location.longitude)
                showToast(R.string.toast_gps_success)
            } else {
                _locationFetchState.value = LocationFetchState.Error(R.string.toast_gps_failed)
                showToast(R.string.toast_gps_failed)
            }
        }
    }

    fun saveLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.saveLocation(lat, lng)
            val lang = repository.languageFlow.firstOrNull()
            val cityName = repository.getPreciseLocationName(lat, lng, lang) ?: "Unknown Location"
            val newFavorite = FavoriteCity(cityName = cityName, lat = lat, lon = lng, isCurrentLocation = true)
            repository.setCityAsCurrent(newFavorite)
            _locationFetchState.value = LocationFetchState.Success(cityName)
            setShowMapDialog(false)
            setLocationMethod("Map")
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