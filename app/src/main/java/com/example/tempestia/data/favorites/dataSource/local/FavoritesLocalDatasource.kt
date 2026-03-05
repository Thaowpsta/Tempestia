package com.example.tempestia.data.favorites.dataSource.local

import android.content.Context
import com.example.tempestia.data.db.AppDatabase
import com.example.tempestia.data.favorites.model.FavoriteCity
import kotlinx.coroutines.flow.Flow

class FavoritesLocalDatasource(context: Context) {
    private val favoriteCityDao = AppDatabase.getDatabase(context).favoriteCityDao()

    fun getAllFavorites(): Flow<List<FavoriteCity>> = favoriteCityDao.getAllFavorites()

    suspend fun insertFavorite(city: FavoriteCity) {
        favoriteCityDao.insertFavorite(city)
    }

    suspend fun deleteFavorite(city: FavoriteCity) {
        favoriteCityDao.deleteFavorite(city)
    }
}