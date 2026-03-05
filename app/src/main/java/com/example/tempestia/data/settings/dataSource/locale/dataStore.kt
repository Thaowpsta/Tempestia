package com.example.tempestia.data.settings.dataSource.locale

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsLocalDatasource(private val context: Context) {

    private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    private val LATITUDE_KEY = doublePreferencesKey("latitude")
    private val LONGITUDE_KEY = doublePreferencesKey("longitude")

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[ONBOARDING_COMPLETED_KEY] ?: false }

    val locationFlow: Flow<Pair<Double, Double>?> = context.dataStore.data
        .map { preferences ->
            val lat = preferences[LATITUDE_KEY]
            val lng = preferences[LONGITUDE_KEY]
            if (lat != null && lng != null) Pair(lat, lng) else null
        }

    suspend fun completeOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = true
        }
    }

    suspend fun saveLocation(lat: Double, lng: Double) {
        context.dataStore.edit { preferences ->
            preferences[LATITUDE_KEY] = lat
            preferences[LONGITUDE_KEY] = lng
        }
    }
}