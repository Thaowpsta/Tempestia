package com.example.tempestia.ui.onboarding.viewModel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "settings")

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore

    private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    private val LATITUDE_KEY = doublePreferencesKey("latitude")
    private val LONGITUDE_KEY = doublePreferencesKey("longitude")

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[ONBOARDING_COMPLETED_KEY] ?: false }

    val locationFlow: Flow<Pair<Double, Double>?> = dataStore.data
        .map { preferences ->
            val lat = preferences[LATITUDE_KEY]
            val lng = preferences[LONGITUDE_KEY]
            if (lat != null && lng != null) Pair(lat, lng) else null
        }

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[ONBOARDING_COMPLETED_KEY] = true
            }
        }
    }

    fun saveLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[LATITUDE_KEY] = lat
                preferences[LONGITUDE_KEY] = lng
            }
        }
    }
}