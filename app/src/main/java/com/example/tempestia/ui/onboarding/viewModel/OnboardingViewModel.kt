package com.example.tempestia.ui.onboarding.viewModel

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tempestia.data.favorites.model.FavoriteCity
import com.example.tempestia.data.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class OnboardingViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _currentScreen = MutableStateFlow(0)
    val currentScreen: StateFlow<Int> = _currentScreen.asStateFlow()

    private val _isFetchingLocation = MutableStateFlow(false)
    val isFetchingLocation: StateFlow<Boolean> = _isFetchingLocation.asStateFlow()

    private val _addressText = MutableStateFlow<String?>(null)
    val addressText: StateFlow<String?> = _addressText.asStateFlow()

    private val _hasDragged = MutableStateFlow(false)
    val hasDragged: StateFlow<Boolean> = _hasDragged.asStateFlow()

    fun setCurrentScreen(screen: Int) { _currentScreen.value = screen }
    fun setIsFetchingLocation(isFetching: Boolean) { _isFetchingLocation.value = isFetching }
    fun setAddressText(text: String?) { _addressText.value = text }
    fun setHasDragged(dragged: Boolean) { _hasDragged.value = dragged }

    val isOnboardingCompleted: Flow<Boolean> = repository.isOnboardingCompleted
    val locationFlow: Flow<Pair<Double, Double>?> = repository.locationFlow

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.completeOnboarding()
        }
    }

    fun saveLocation(lat: Double, lng: Double, knownCityName: String? = null) {
        viewModelScope.launch {
            repository.saveLocation(lat, lng)

            val cityName = knownCityName ?: repository.getPreciseLocationName(lat, lng) ?: "Unknown Location"
            val newFavorite = FavoriteCity(
                cityName = cityName,
                lat = lat,
                lon = lng,
                isCurrentLocation = true
            )
            repository.setCityAsCurrent(newFavorite)
        }
    }

    fun fetchAddressFromLatLng(context: Context, lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)

                if (!addresses.isNullOrEmpty()) {
                    val addressLine = addresses[0].getAddressLine(0)
                    _addressText.value = addressLine
                } else {
                    _addressText.value = String.format(Locale.US, "Lat: %.4f, Lng: %.4f", lat, lng)
                }
            } catch (e: Exception) {
                android.util.Log.e("OnboardingViewModel", "Geocoder failed to find address", e)
                _addressText.value = String.format(Locale.US, "Lat: %.4f, Lng: %.4f", lat, lng)
            }
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