package com.example.tempestia.repository

import android.content.Context
import android.location.Geocoder
import com.example.tempestia.data.alerts.dataSource.locale.AlertsLocalDatasource
import com.example.tempestia.data.alerts.model.Alert
import com.example.tempestia.data.favorites.dataSource.local.FavoritesLocalDatasource
import com.example.tempestia.data.favorites.model.FavoriteCity
import com.example.tempestia.data.settings.dataSource.locale.SettingsLocalDatasource
import com.example.tempestia.data.weather.dataSource.remote.WeatherRemoteDatasource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale

class WeatherRepository(private val context: Context) {
    private val remoteDatasource = WeatherRemoteDatasource()
    private val FavoriteslocalDatasource = FavoritesLocalDatasource(context)
    private val onboardingLocalDatasource = SettingsLocalDatasource(context)
    private val alertsLocalDataSource = AlertsLocalDatasource(context)

    val isOnboardingCompleted = onboardingLocalDatasource.isOnboardingCompleted
    val locationFlow = onboardingLocalDatasource.locationFlow

    val isCelsiusFlow = onboardingLocalDatasource.isCelsiusFlow
    val is24HourFlow = onboardingLocalDatasource.is24HourFlow
    val themeModeFlow = onboardingLocalDatasource.themeModeFlow

    suspend fun getPreciseLocationName(lat: Double, lon: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // Priority: Neighborhood -> City -> Region
                    address.subLocality ?: address.locality ?: address.adminArea
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun completeOnboarding() = onboardingLocalDatasource.completeOnboarding()

    suspend fun saveLocation(lat: Double, lng: Double) = onboardingLocalDatasource.saveLocation(lat, lng)

    suspend fun getWeather(lat: Double, lon: Double, apiKey: String) =
        remoteDatasource.getCurrentWeather(lat, lon, apiKey)
    suspend fun getCityName(lat: Double, lon: Double, apiKey: String) =
        remoteDatasource.getCityName(lat, lon, apiKey)
    suspend fun getCoordinatesByName(query: String, apiKey: String, limit: Int = 5) =
        remoteDatasource.getCoordinatesByName(query, apiKey, limit)

    fun getFavoriteCities(): Flow<List<FavoriteCity>> = FavoriteslocalDatasource.getAllFavorites()
    suspend fun insertFavorite(city: FavoriteCity) = FavoriteslocalDatasource.insertFavorite(city)
    suspend fun deleteFavorite(city: FavoriteCity) = FavoriteslocalDatasource.deleteFavorite(city)

    fun getSubscribedAlerts(): Flow<List<Alert>> = alertsLocalDataSource.getAllAlerts()
    suspend fun insertAlert(alert: Alert) = alertsLocalDataSource.insertAlert(alert)
    suspend fun deleteAlert(id: String) = alertsLocalDataSource.deleteAlert(id)

    suspend fun saveIsCelsius(isCelsius: Boolean) = onboardingLocalDatasource.saveIsCelsius(isCelsius)
    suspend fun saveIs24Hour(is24Hour: Boolean) = onboardingLocalDatasource.saveIs24Hour(is24Hour)
    suspend fun saveThemeMode(themeMode: String) = onboardingLocalDatasource.saveThemeMode(themeMode)
}