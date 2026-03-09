package com.example.tempestia.data.settings.dataSource.locale

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsLocalDatasource(private val context: Context) {

    private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    private val LATITUDE_KEY = doublePreferencesKey("latitude")
    private val LONGITUDE_KEY = doublePreferencesKey("longitude")
    private val IS_CELSIUS_KEY = booleanPreferencesKey("is_celsius")
    private val IS_24_HOUR_KEY = booleanPreferencesKey("is_24_hour")
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED_KEY] ?: false }
    val locationFlow: Flow<Pair<Double, Double>?> = context.dataStore.data.map {
        val lat = it[LATITUDE_KEY]
        val lng = it[LONGITUDE_KEY]
        if (lat != null && lng != null) Pair(lat, lng) else null
    }

    val isCelsiusFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_CELSIUS_KEY] ?: true }
    val is24HourFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_24_HOUR_KEY] ?: false }
    val themeModeFlow: Flow<String> = context.dataStore.data.map { it[THEME_MODE_KEY] ?: "System" }

    suspend fun completeOnboarding() { context.dataStore.edit { it[ONBOARDING_COMPLETED_KEY] = true } }
    suspend fun saveLocation(lat: Double, lng: Double) {
        context.dataStore.edit {
            it[LATITUDE_KEY] = lat
            it[LONGITUDE_KEY] = lng
        }
    }

    suspend fun saveIsCelsius(isCelsius: Boolean) { context.dataStore.edit { it[IS_CELSIUS_KEY] = isCelsius } }
    suspend fun saveIs24Hour(is24Hour: Boolean) { context.dataStore.edit { it[IS_24_HOUR_KEY] = is24Hour } }
    suspend fun saveThemeMode(themeMode: String) { context.dataStore.edit { it[THEME_MODE_KEY] = themeMode } }
}