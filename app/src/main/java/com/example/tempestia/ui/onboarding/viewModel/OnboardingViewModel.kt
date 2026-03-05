package com.example.tempestia.ui.onboarding.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tempestia.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OnboardingViewModel(private val repository: WeatherRepository) : ViewModel() {

    val isOnboardingCompleted: Flow<Boolean> = repository.isOnboardingCompleted
    val locationFlow: Flow<Pair<Double, Double>?> = repository.locationFlow

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.completeOnboarding()
        }
    }

    fun saveLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.saveLocation(lat, lng)
        }
    }
}

class OnboardingViewModelFactory(
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}