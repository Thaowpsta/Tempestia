package com.example.tempestia.data.favorites.dataSource.local

import com.example.tempestia.data.favorites.model.FavoriteCity
import kotlinx.coroutines.flow.Flow

class FavoritesLocalDatasource(private val favoriteCityDao: FavoriteCityDao) {

    fun getAllFavorites(): Flow<List<FavoriteCity>> = favoriteCityDao.getAllFavorites()

    suspend fun getCityByLatLng(lat: Double, lon: Double) =
        favoriteCityDao.getCityByLatLng(lat, lon)

    suspend fun getAllFavoritesSync(): List<FavoriteCity> = favoriteCityDao.getAllFavoritesSync()

    suspend fun insertFavorite(city: FavoriteCity) {
        favoriteCityDao.insertFavorite(city)
    }

    suspend fun updateFavorite(city: FavoriteCity) {
        favoriteCityDao.updateFavorite(city)
    }

    suspend fun deleteFavorite(city: FavoriteCity) {
        favoriteCityDao.deleteFavorite(city)
    }

    suspend fun setCityAsCurrent(city: FavoriteCity) {
        favoriteCityDao.setCityAsCurrent(city)
    }
}