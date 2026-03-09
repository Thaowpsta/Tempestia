package com.example.tempestia.ui.settings.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tempestia.repository.WeatherRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: WeatherRepository) : ViewModel() {

    val isCelsiusFlow = repository.isCelsiusFlow
    val is24HourFlow = repository.is24HourFlow
    val themeModeFlow = repository.themeModeFlow

    fun setCelsius(isCelsius: Boolean) {
        viewModelScope.launch { repository.saveIsCelsius(isCelsius) }
    }

    fun set24Hour(is24Hour: Boolean) {
        viewModelScope.launch { repository.saveIs24Hour(is24Hour) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.saveThemeMode(mode) }
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