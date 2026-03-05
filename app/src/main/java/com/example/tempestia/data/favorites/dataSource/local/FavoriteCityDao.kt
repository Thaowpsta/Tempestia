package com.example.tempestia.data.favorites.dataSource.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tempestia.data.favorites.model.FavoriteCity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCityDao {
    @Query("SELECT * FROM favorite_cities ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteCity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(city: FavoriteCity)

    @Delete
    suspend fun deleteFavorite(city: FavoriteCity)
}