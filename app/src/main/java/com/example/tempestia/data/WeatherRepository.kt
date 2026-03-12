package com.example.tempestia.repository

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.example.tempestia.data.alerts.dataSource.locale.AlertsLocalDatasource
import com.example.tempestia.data.alerts.model.Alert
import com.example.tempestia.data.favorites.dataSource.local.FavoritesLocalDatasource
import com.example.tempestia.data.favorites.model.FavoriteCity
import com.example.tempestia.data.settings.dataSource.locale.LocationDataSource
import com.example.tempestia.data.settings.dataSource.locale.SettingsLocalDatasource
import com.example.tempestia.data.weather.dataSource.remote.WeatherRemoteDatasource
import com.example.tempestia.utils.NetworkUtils.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale

class WeatherRepository(
    private val context: Context,
    private val remoteDatasource: WeatherRemoteDatasource = WeatherRemoteDatasource(),
    private val favoritesLocalDatasource: FavoritesLocalDatasource = FavoritesLocalDatasource(context),
    private val alertsLocalDatasource: AlertsLocalDatasource = AlertsLocalDatasource(context),
    private val settingsLocalDatasource: SettingsLocalDatasource = SettingsLocalDatasource(context)
) {

    val isOnboardingCompleted = settingsLocalDatasource.isOnboardingCompleted
    val locationFlow = settingsLocalDatasource.locationFlow

    val isCelsiusFlow = settingsLocalDatasource.isCelsiusFlow
    val is24HourFlow = settingsLocalDatasource.is24HourFlow
    val themeModeFlow = settingsLocalDatasource.themeModeFlow

    val languageFlow = settingsLocalDatasource.languageFlow

    suspend fun getPreciseLocationName(lat: Double, lon: Double, languageTag: String? = null): String? {
        return withContext(Dispatchers.IO) {
            try {
                val locale = if (languageTag != null) Locale(languageTag) else Locale.getDefault()
                val geocoder = Geocoder(context, locale)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    address.subLocality ?: address.locality ?: address.adminArea
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun completeOnboarding() = settingsLocalDatasource.completeOnboarding()

    suspend fun saveLocation(lat: Double, lng: Double) = settingsLocalDatasource.saveLocation(lat, lng)

    suspend fun getWeather(lat: Double, lon: Double, apiKey: String) =
        remoteDatasource.getCurrentWeather(lat, lon, apiKey)
    suspend fun getCityName(lat: Double, lon: Double, apiKey: String) =
        remoteDatasource.getCityName(lat, lon, apiKey)
    suspend fun getCoordinatesByName(query: String, apiKey: String, limit: Int = 5) =
        remoteDatasource.getCoordinatesByName(query, apiKey, limit)

    fun getFavoriteCities(): Flow<List<FavoriteCity>> = favoritesLocalDatasource.getAllFavorites()

    suspend fun getAllFavoritesSync(): List<FavoriteCity> = favoritesLocalDatasource.getAllFavoritesSync()
    suspend fun updateFavorite(city: FavoriteCity) = favoritesLocalDatasource.updateFavorite(city)
    suspend fun getCityByLatLng(lat: Double, lon: Double) = favoritesLocalDatasource.getCityByLatLng(lat, lon)

    suspend fun insertFavorite(city: FavoriteCity) = favoritesLocalDatasource.insertFavorite(city)
    suspend fun deleteFavorite(city: FavoriteCity) = favoritesLocalDatasource.deleteFavorite(city)
    suspend fun setCityAsCurrent(city: FavoriteCity) = favoritesLocalDatasource.setCityAsCurrent(city)
    fun getSubscribedAlerts(): Flow<List<Alert>> = alertsLocalDatasource.getAllAlerts()
    suspend fun insertAlert(alert: Alert) = alertsLocalDatasource.insertAlert(alert)
    suspend fun deleteAlert(id: String) = alertsLocalDatasource.deleteAlert(id)

    suspend fun saveIsCelsius(isCelsius: Boolean) = settingsLocalDatasource.saveIsCelsius(isCelsius)
    suspend fun saveIs24Hour(is24Hour: Boolean) = settingsLocalDatasource.saveIs24Hour(is24Hour)
    suspend fun saveThemeMode(themeMode: String) = settingsLocalDatasource.saveThemeMode(themeMode)

    suspend fun saveLanguage(language: String) = settingsLocalDatasource.saveLanguage(language)

    fun isNetworkAvailable(): Boolean {
        return isNetworkAvailable(context)
    }

    suspend fun fetchDeviceLocation(): Location? {
        val tracker = LocationDataSource(context)
        return tracker.getCurrentLocation()
    }
}