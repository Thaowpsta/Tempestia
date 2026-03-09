package com.example.tempestia.data.favorites.dataSource.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.tempestia.data.favorites.model.FavoriteCity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCityDao {
    @Query("SELECT * FROM favorite_cities ORDER BY isCurrentLocation DESC, addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteCity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(city: FavoriteCity)

    @Delete
    suspend fun deleteFavorite(city: FavoriteCity)

    @Query("UPDATE favorite_cities SET isCurrentLocation = 0")
    suspend fun clearAllCurrentFlags()

    @Transaction
    suspend fun setCityAsCurrent(city: FavoriteCity) {
        clearAllCurrentFlags()
        insertFavorite(city.copy(isCurrentLocation = true))
    }
}