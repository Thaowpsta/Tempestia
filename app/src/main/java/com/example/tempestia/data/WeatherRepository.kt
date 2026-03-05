package com.example.tempestia.repository

import android.content.Context
import com.example.tempestia.data.favorites.dataSource.local.FavoritesLocalDatasource
import com.example.tempestia.data.favorites.model.FavoriteCity
import com.example.tempestia.data.settings.dataSource.locale.SettingsLocalDatasource
import com.example.tempestia.data.weather.dataSource.remote.WeatherRemoteDatasource
import kotlinx.coroutines.flow.Flow

class WeatherRepository(context: Context) {
    private val remoteDatasource = WeatherRemoteDatasource()
    private val FavoriteslocalDatasource = FavoritesLocalDatasource(context)

    private val onboardingLocalDatasource = SettingsLocalDatasource(context)

    val isOnboardingCompleted = onboardingLocalDatasource.isOnboardingCompleted
    val locationFlow = onboardingLocalDatasource.locationFlow

    suspend fun completeOnboarding() = onboardingLocalDatasource.completeOnboarding()

    suspend fun saveLocation(lat: Double, lng: Double) = onboardingLocalDatasource.saveLocation(lat, lng)

    suspend fun getWeather(lat: Double, lon: Double, apiKey: String) =
        remoteDatasource.getCurrentWeather(lat, lon, apiKey)

    suspend fun getCityName(lat: Double, lon: Double, apiKey: String) =
        remoteDatasource.getCityName(lat, lon, apiKey)

    fun getFavoriteCities(): Flow<List<FavoriteCity>> = FavoriteslocalDatasource.getAllFavorites()

    suspend fun insertFavorite(city: FavoriteCity) = FavoriteslocalDatasource.insertFavorite(city)

    suspend fun deleteFavorite(city: FavoriteCity) = FavoriteslocalDatasource.deleteFavorite(city)
}